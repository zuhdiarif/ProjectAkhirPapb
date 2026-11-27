"""Train and export the Project Harmony neural network for AURA (Python -> .tflite + Kotlin weights)."""

from __future__ import annotations

import json
import math
from pathlib import Path
import numpy as np
import flatbuffers
import tflite

SEED = 42
INPUT_SIZE = 22
HIDDEN_1 = 48
HIDDEN_2 = 24
OUTPUT_SIZE = 7
CLASS_COUNT = 6

HARMONY_LABELS = [
    "Harmoni Netral Klasik",
    "Paduan Netral Seimbang",
    "Harmoni Monokromatik",
    "Harmoni Analogus",
    "Harmoni Komplementer",
    "Kontras Warna Seimbang",
]


def is_neutral_hsv(s: float, v: float) -> bool:
    return s < 0.16 or v < 0.16


def circular_hue_diff(h1: float, h2: float) -> float:
    raw = abs((h1 % 360.0) - (h2 % 360.0))
    return min(raw, 360.0 - raw)


def encode_garment_vector(h: float, s: float, v: float, pattern_load: float) -> list[float]:
    rad = 2.0 * math.pi * (h % 360.0) / 360.0
    s_c = max(0.0, min(1.0, s))
    v_c = max(0.0, min(1.0, v))
    neutral = 1.0 if is_neutral_hsv(s_c, v_c) else 0.0
    return [math.sin(rad), math.cos(rad), s_c, v_c, neutral, pattern_load]


def encode_outfit(
    top: tuple[float, float, float, float],
    bottom: tuple[float, float, float, float],
    outer: tuple[float, float, float, float] | None,
) -> np.ndarray:
    vec = np.zeros(INPUT_SIZE, dtype=np.float32)
    vec[0:6] = encode_garment_vector(*top)
    vec[6:12] = encode_garment_vector(*bottom)
    garments = [top, bottom]
    if outer is not None:
        vec[12:18] = encode_garment_vector(*outer)
        vec[18] = 1.0
        garments.append(outer)

    chromatics = [g for g in garments if not is_neutral_hsv(g[1], g[2])]
    if len(chromatics) >= 2:
        diffs = []
        for i in range(len(chromatics)):
            for j in range(i + 1, len(chromatics)):
                diffs.append(circular_hue_diff(chromatics[i][0], chromatics[j][0]))
        vec[19] = min(diffs) / 180.0
        vec[20] = max(diffs) / 180.0
    vec[21] = abs(max(0.0, min(1.0, top[2])) - max(0.0, min(1.0, bottom[2])))
    return vec


def ground_truth_label_and_score(
    top: tuple[float, float, float, float],
    bottom: tuple[float, float, float, float],
    outer: tuple[float, float, float, float] | None,
) -> tuple[int, float]:
    garments = [top, bottom] + ([outer] if outer is not None else [])
    chromatics = [g for g in garments if not is_neutral_hsv(g[1], g[2])]
    val_contrast = abs(top[2] - bottom[2])

    if len(chromatics) == 0:
        cls_idx = 0
        base = 0.86 + 0.05 * min(1.0, val_contrast / 0.65)
    elif len(chromatics) == 1:
        cls_idx = 1
        base = 0.83 + 0.05 * min(1.0, val_contrast / 0.65)
    else:
        diffs = [
            circular_hue_diff(chromatics[i][0], chromatics[j][0])
            for i in range(len(chromatics))
            for j in range(i + 1, len(chromatics))
        ]
        max_diff = max(diffs)
        if max_diff <= 15.0:
            cls_idx = 2
            base = 0.92 + 0.04 * min(1.0, val_contrast / 0.50)
        elif max_diff <= 40.0:
            cls_idx = 3
            base = 0.935
        elif any(d >= 145.0 for d in diffs):
            cls_idx = 4
            base = 0.90
        else:
            cls_idx = 5
            base = 0.78 if max_diff <= 75.0 else 0.62

    loud_patterns = sum(1 for g in garments if g[3] >= 0.9)
    solid_count = sum(1 for g in garments if g[3] <= 0.1)
    if loud_patterns >= 2:
        base -= 0.08
    elif loud_patterns == 1 and solid_count >= 1:
        base += 0.02

    score = float(np.clip(base, 0.35, 0.98))
    return cls_idx, score


def sample_garment(rng: np.random.Generator, force_mode: str | None = None, base_hue: float | None = None):
    mode = force_mode
    if mode is None:
        r = rng.random()
        if r < 0.32:
            mode = "neutral"
        else:
            mode = "chromatic"
    if mode == "neutral":
        if rng.random() < 0.5:
            h = float(rng.uniform(0.0, 360.0))
            s = float(rng.uniform(0.0, 0.145))
            v = float(rng.uniform(0.18, 0.99))
        else:
            h = float(rng.uniform(0.0, 360.0))
            s = float(rng.uniform(0.0, 0.70))
            v = float(rng.uniform(0.03, 0.145))
    else:
        h = float(base_hue % 360.0) if base_hue is not None else float(rng.uniform(0.0, 360.0))
        s = float(rng.uniform(0.18, 0.96))
        v = float(rng.uniform(0.20, 0.98))
    pattern = float(rng.choice([0.0, 0.0, 0.0, 0.5, 1.0]))
    return (h, s, v, pattern)


def build_dataset(n_samples: int = 24000, seed: int = SEED):
    rng = np.random.default_rng(seed)
    X = np.zeros((n_samples, INPUT_SIZE), dtype=np.float32)
    y_cls = np.zeros(n_samples, dtype=np.int64)
    y_score = np.zeros(n_samples, dtype=np.float32)

    for idx in range(n_samples):
        has_outer = rng.random() < 0.50
        bucket = idx % 6
        base_h = float(rng.uniform(0.0, 360.0))
        if bucket == 0:  # All neutral
            top = sample_garment(rng, "neutral")
            bottom = sample_garment(rng, "neutral")
            outer = sample_garment(rng, "neutral") if has_outer else None
        elif bucket == 1:  # One chromatic + neutral(s)
            slots = ["chromatic", "neutral"] + (["neutral"] if has_outer else [])
            rng.shuffle(slots)
            top = sample_garment(rng, slots[0])
            bottom = sample_garment(rng, slots[1])
            outer = sample_garment(rng, slots[2]) if has_outer else None
        elif bucket == 2:  # Monochromatic (delta <= 15)
            d1 = float(rng.uniform(-14.5, 14.5))
            d2 = float(rng.uniform(-14.5, 14.5))
            top = sample_garment(rng, "chromatic", base_h)
            bottom = sample_garment(rng, "chromatic", base_h + d1)
            outer = sample_garment(rng, "chromatic" if rng.random() < 0.5 else "neutral", base_h + d2) if has_outer else None
        elif bucket == 3:  # Analogous (15 < maxDiff <= 40)
            sign = 1.0 if rng.random() < 0.5 else -1.0
            d1 = sign * float(rng.uniform(16.0, 39.5))
            top = sample_garment(rng, "chromatic", base_h)
            bottom = sample_garment(rng, "chromatic", base_h + d1)
            outer = sample_garment(rng, "neutral") if has_outer else None
        elif bucket == 4:  # Complementary (diff >= 145)
            d1 = float(rng.uniform(146.0, 180.0)) * (1.0 if rng.random() < 0.5 else -1.0)
            top = sample_garment(rng, "chromatic", base_h)
            bottom = sample_garment(rng, "chromatic", base_h + d1)
            outer = sample_garment(rng, "neutral") if has_outer else None
        else:  # Balanced contrast / general random
            d1 = float(rng.uniform(41.0, 144.0)) * (1.0 if rng.random() < 0.5 else -1.0)
            top = sample_garment(rng, "chromatic", base_h)
            bottom = sample_garment(rng, "chromatic", base_h + d1)
            outer = sample_garment(rng) if has_outer else None

        X[idx] = encode_outfit(top, bottom, outer)
        cls_id, score = ground_truth_label_and_score(top, bottom, outer)
        y_cls[idx] = cls_id
        y_score[idx] = score

    return X, y_cls, y_score


def relu(x: np.ndarray) -> np.ndarray:
    return np.maximum(0.0, x)


def softmax(logits: np.ndarray) -> np.ndarray:
    shifted = logits - np.max(logits, axis=1, keepdims=True)
    exps = np.exp(shifted)
    return exps / np.sum(exps, axis=1, keepdims=True)


def sigmoid(z: np.ndarray) -> np.ndarray:
    z_c = np.clip(z, -20.0, 20.0)
    return 1.0 / (1.0 + np.exp(-z_c))


def train_mlp(X: np.ndarray, y_cls: np.ndarray, y_score: np.ndarray):
    rng = np.random.default_rng(SEED)
    perm = rng.permutation(len(X))
    split = int(len(X) * 0.85)
    tr_idx, va_idx = perm[:split], perm[split:]
    X_tr, y_cls_tr, y_sc_tr = X[tr_idx], y_cls[tr_idx], y_score[tr_idx]
    X_va, y_cls_va, y_sc_va = X[va_idx], y_cls[va_idx], y_score[va_idx]

    params = {
        "W1": (rng.normal(0, math.sqrt(2.0 / INPUT_SIZE), (INPUT_SIZE, HIDDEN_1))).astype(np.float64),
        "b1": np.zeros(HIDDEN_1, dtype=np.float64),
        "W2": (rng.normal(0, math.sqrt(2.0 / HIDDEN_1), (HIDDEN_1, HIDDEN_2))).astype(np.float64),
        "b2": np.zeros(HIDDEN_2, dtype=np.float64),
        "W3": (rng.normal(0, math.sqrt(2.0 / HIDDEN_2), (HIDDEN_2, OUTPUT_SIZE))).astype(np.float64),
        "b3": np.zeros(OUTPUT_SIZE, dtype=np.float64),
    }

    m = {k: np.zeros_like(v) for k, v in params.items()}
    v = {k: np.zeros_like(w) for k, w in params.items()}
    beta1, beta2, eps = 0.9, 0.999, 1e-8
    batch_size = 256
    epochs = 140
    step = 0

    for epoch in range(epochs):
        order = rng.permutation(len(X_tr))
        lr = 0.004 * (0.5 ** (epoch // 45))
        for start in range(0, len(X_tr), batch_size):
            b_idx = order[start : start + batch_size]
            xb = X_tr[b_idx].astype(np.float64)
            yc = y_cls_tr[b_idx]
            ys = y_sc_tr[b_idx].astype(np.float64)
            bs = len(xb)

            # Forward
            z1 = xb @ params["W1"] + params["b1"]
            a1 = relu(z1)
            z2 = a1 @ params["W2"] + params["b2"]
            a2 = relu(z2)
            out = a2 @ params["W3"] + params["b3"]

            cls_logits = out[:, :CLASS_COUNT]
            cls_probs = softmax(cls_logits)
            score_pred = sigmoid(out[:, 6])

            # Gradients
            d_cls = cls_probs.copy()
            d_cls[np.arange(bs), yc] -= 1.0
            d_cls /= bs

            # Binary cross-entropy on continuous target + MSE boost
            d_score = (6.0 * (score_pred - ys) / bs)

            d_out = np.zeros_like(out)
            d_out[:, :CLASS_COUNT] = d_cls
            d_out[:, 6] = d_score

            grads = {}
            grads["W3"] = a2.T @ d_out + 1e-5 * params["W3"]
            grads["b3"] = np.sum(d_out, axis=0)

            d_a2 = d_out @ params["W3"].T
            d_z2 = d_a2 * (z2 > 0)
            grads["W2"] = a1.T @ d_z2 + 1e-5 * params["W2"]
            grads["b2"] = np.sum(d_z2, axis=0)

            d_a1 = d_z2 @ params["W2"].T
            d_z1 = d_a1 * (z1 > 0)
            grads["W1"] = xb.T @ d_z1 + 1e-5 * params["W1"]
            grads["b1"] = np.sum(d_z1, axis=0)

            step += 1
            for k in params:
                m[k] = beta1 * m[k] + (1.0 - beta1) * grads[k]
                v[k] = beta2 * v[k] + (1.0 - beta2) * (grads[k] ** 2)
                m_hat = m[k] / (1.0 - beta1 ** step)
                v_hat = v[k] / (1.0 - beta2 ** step)
                params[k] -= lr * m_hat / (np.sqrt(v_hat) + eps)

    # Validation metrics (evaluated in float32 to match LiteRT / Kotlin inference)
    f32_params = {k: arr.astype(np.float32) for k, arr in params.items()}
    va_logits = forward_float32(X_va, f32_params)
    pred_cls = np.argmax(va_logits[:, :CLASS_COUNT], axis=1)
    pred_score = 1.0 / (1.0 + np.exp(-np.clip(va_logits[:, 6], -20.0, 20.0)))
    val_acc = float(np.mean(pred_cls == y_cls_va))
    val_mae = float(np.mean(np.abs(pred_score - y_sc_va)))
    return f32_params, val_acc, val_mae


def forward_float32(X: np.ndarray, p: dict[str, np.ndarray]) -> np.ndarray:
    a1 = np.maximum(0.0, X.astype(np.float32) @ p["W1"] + p["b1"]).astype(np.float32)
    a2 = np.maximum(0.0, a1 @ p["W2"] + p["b2"]).astype(np.float32)
    out = (a2 @ p["W3"] + p["b3"]).astype(np.float32)
    return out


def build_tflite_model_bytes(params: dict[str, np.ndarray]) -> bytes:
    """Serialize 3-layer FULLY_CONNECTED MLP into TFLite v3 FlatBuffer format."""
    builder = flatbuffers.Builder(65536)

    def create_buffer(array: np.ndarray | None) -> int:
        if array is None:
            tflite.BufferStart(builder)
            return tflite.BufferEnd(builder)
        raw = np.ascontiguousarray(array, dtype=np.float32).tobytes()
        data_vec = builder.CreateByteVector(raw)
        tflite.BufferStart(builder)
        tflite.BufferAddData(builder, data_vec)
        return tflite.BufferEnd(builder)

    # Buffers: 0 empty, 1..6 weights & biases
    # TFLite FULLY_CONNECTED expects weights shaped [out_features, in_features].
    w1_t = np.ascontiguousarray(params["W1"].T, dtype=np.float32)
    b1 = np.ascontiguousarray(params["b1"], dtype=np.float32)
    w2_t = np.ascontiguousarray(params["W2"].T, dtype=np.float32)
    b2 = np.ascontiguousarray(params["b2"], dtype=np.float32)
    w3_t = np.ascontiguousarray(params["W3"].T, dtype=np.float32)
    b3 = np.ascontiguousarray(params["b3"], dtype=np.float32)

    buf_offsets = [
        create_buffer(None),
        create_buffer(w1_t),
        create_buffer(b1),
        create_buffer(w2_t),
        create_buffer(b2),
        create_buffer(w3_t),
        create_buffer(b3),
    ]
    tflite.ModelStartBuffersVector(builder, len(buf_offsets))
    for off in reversed(buf_offsets):
        builder.PrependUOffsetTRelative(off)
    buffers_vec = builder.EndVector()

    def create_tensor(name: str, shape: list[int], buf_idx: int) -> int:
        name_off = builder.CreateString(name)
        tflite.TensorStartShapeVector(builder, len(shape))
        for d in reversed(shape):
            builder.PrependInt32(d)
        shape_vec = builder.EndVector()

        tflite.TensorStart(builder)
        tflite.TensorAddShape(builder, shape_vec)
        tflite.TensorAddType(builder, tflite.TensorType.FLOAT32)
        tflite.TensorAddBuffer(builder, buf_idx)
        tflite.TensorAddName(builder, name_off)
        return tflite.TensorEnd(builder)

    tensors = [
        create_tensor("outfit_features", [1, INPUT_SIZE], 0),       # 0
        create_tensor("fc1/weights", [HIDDEN_1, INPUT_SIZE], 1),    # 1
        create_tensor("fc1/bias", [HIDDEN_1], 2),                   # 2
        create_tensor("fc1/relu", [1, HIDDEN_1], 0),                # 3
        create_tensor("fc2/weights", [HIDDEN_2, HIDDEN_1], 3),      # 4
        create_tensor("fc2/bias", [HIDDEN_2], 4),                   # 5
        create_tensor("fc2/relu", [1, HIDDEN_2], 0),                # 6
        create_tensor("fc3/weights", [OUTPUT_SIZE, HIDDEN_2], 5),   # 7
        create_tensor("fc3/bias", [OUTPUT_SIZE], 6),                # 8
        create_tensor("harmony_logits", [1, OUTPUT_SIZE], 0),       # 9
    ]
    tflite.SubGraphStartTensorsVector(builder, len(tensors))
    for off in reversed(tensors):
        builder.PrependUOffsetTRelative(off)
    tensors_vec = builder.EndVector()

    def create_fc_op(in_indices: list[int], out_indices: list[int], activation: int) -> int:
        tflite.OperatorStartInputsVector(builder, len(in_indices))
        for i in reversed(in_indices):
            builder.PrependInt32(i)
        in_vec = builder.EndVector()

        tflite.OperatorStartOutputsVector(builder, len(out_indices))
        for o in reversed(out_indices):
            builder.PrependInt32(o)
        out_vec = builder.EndVector()

        tflite.FullyConnectedOptionsStart(builder)
        tflite.FullyConnectedOptionsAddFusedActivationFunction(builder, activation)
        tflite.FullyConnectedOptionsAddWeightsFormat(
            builder, tflite.FullyConnectedOptionsWeightsFormat.DEFAULT
        )
        tflite.FullyConnectedOptionsAddKeepNumDims(builder, False)
        fc_opts = tflite.FullyConnectedOptionsEnd(builder)

        tflite.OperatorStart(builder)
        tflite.OperatorAddOpcodeIndex(builder, 0)
        tflite.OperatorAddInputs(builder, in_vec)
        tflite.OperatorAddOutputs(builder, out_vec)
        tflite.OperatorAddBuiltinOptionsType(builder, tflite.BuiltinOptions.FullyConnectedOptions)
        tflite.OperatorAddBuiltinOptions(builder, fc_opts)
        return tflite.OperatorEnd(builder)

    ops = [
        create_fc_op([0, 1, 2], [3], tflite.ActivationFunctionType.RELU),
        create_fc_op([3, 4, 5], [6], tflite.ActivationFunctionType.RELU),
        create_fc_op([6, 7, 8], [9], tflite.ActivationFunctionType.NONE),
    ]
    tflite.SubGraphStartOperatorsVector(builder, len(ops))
    for off in reversed(ops):
        builder.PrependUOffsetTRelative(off)
    ops_vec = builder.EndVector()

    tflite.SubGraphStartInputsVector(builder, 1)
    builder.PrependInt32(0)
    sg_inputs = builder.EndVector()

    tflite.SubGraphStartOutputsVector(builder, 1)
    builder.PrependInt32(9)
    sg_outputs = builder.EndVector()

    sg_name = builder.CreateString("main")
    tflite.SubGraphStart(builder)
    tflite.SubGraphAddTensors(builder, tensors_vec)
    tflite.SubGraphAddInputs(builder, sg_inputs)
    tflite.SubGraphAddOutputs(builder, sg_outputs)
    tflite.SubGraphAddOperators(builder, ops_vec)
    tflite.SubGraphAddName(builder, sg_name)
    subgraph = tflite.SubGraphEnd(builder)

    tflite.ModelStartSubgraphsVector(builder, 1)
    builder.PrependUOffsetTRelative(subgraph)
    subgraphs_vec = builder.EndVector()

    tflite.OperatorCodeStart(builder)
    tflite.OperatorCodeAddDeprecatedBuiltinCode(builder, tflite.BuiltinOperator.FULLY_CONNECTED)
    tflite.OperatorCodeAddVersion(builder, 1)
    tflite.OperatorCodeAddBuiltinCode(builder, tflite.BuiltinOperator.FULLY_CONNECTED)
    op_code = tflite.OperatorCodeEnd(builder)

    tflite.ModelStartOperatorCodesVector(builder, 1)
    builder.PrependUOffsetTRelative(op_code)
    op_codes_vec = builder.EndVector()

    desc = builder.CreateString("AURA Project Harmony Neural Color & Style Scorer v1")
    tflite.ModelStart(builder)
    tflite.ModelAddVersion(builder, 3)
    tflite.ModelAddOperatorCodes(builder, op_codes_vec)
    tflite.ModelAddSubgraphs(builder, subgraphs_vec)
    tflite.ModelAddDescription(builder, desc)
    tflite.ModelAddBuffers(builder, buffers_vec)
    model = tflite.ModelEnd(builder)

    builder.Finish(model, file_identifier=b"TFL3")
    return bytes(builder.Output())


def format_float_array(name: str, arr: np.ndarray) -> str:
    flat = arr.flatten().tolist()
    chunks = []
    for i in range(0, len(flat), 8):
        line = ", ".join(f"{val:.7f}f" for val in flat[i : i + 8])
        chunks.append(f"        {line}")
    return f"    val {name} = floatArrayOf(\n" + ",\n".join(chunks) + "\n    )"


def generate_kotlin_weights(params: dict[str, np.ndarray], target_path: Path) -> None:
    content = "\n".join(
        [
            "package com.zuhdiarifazizi.projectakhirpapb.data.harmony",
            "",
            "// Auto-generated by ml/project_harmony/train_project_harmony.py (do not hand-edit).",
            "internal object ProjectHarmonyWeights {",
            f"    const val INPUT_SIZE = {INPUT_SIZE}",
            f"    const val HIDDEN_1 = {HIDDEN_1}",
            f"    const val HIDDEN_2 = {HIDDEN_2}",
            f"    const val OUTPUT_SIZE = {OUTPUT_SIZE}",
            "",
            format_float_array("W1", params["W1"].T),
            "",
            format_float_array("B1", params["b1"]),
            "",
            format_float_array("W2", params["W2"].T),
            "",
            format_float_array("B2", params["b2"]),
            "",
            format_float_array("W3", params["W3"].T),
            "",
            format_float_array("B3", params["b3"]),
            "}",
            "",
        ]
    )
    target_path.parent.mkdir(parents=True, exist_ok=True)
    target_path.write_text(content, encoding="utf-8")


def build_golden_vectors(params: dict[str, np.ndarray]) -> list[dict]:
    cases = [
        ("neutral_classic", (0.0, 0.02, 0.98, 0.0), (0.0, 0.02, 0.08, 0.0), None),
        ("neutral_plus_denim", (0.0, 0.02, 0.98, 0.0), (211.0, 0.54, 0.56, 0.0), None),
        ("monochromatic_navy_blue", (220.0, 0.82, 0.25, 0.0), (211.0, 0.54, 0.56, 0.0), None),
        ("analogous_olive_cream", (80.0, 0.55, 0.38, 0.0), (50.0, 0.45, 0.80, 0.0), None),
        ("complementary_blue_terracotta", (215.0, 0.72, 0.48, 0.0), (24.0, 0.65, 0.68, 0.0), None),
    ]
    out = []
    for name, top, bottom, outer in cases:
        vec = encode_outfit(top, bottom, outer)
        logits = forward_float32(vec.reshape(1, -1), params)[0]
        probs = softmax(logits[:CLASS_COUNT].reshape(1, -1))[0]
        score = float(1.0 / (1.0 + math.exp(-max(-20.0, min(20.0, float(logits[6]))))))
        out.append(
            {
                "name": name,
                "input": [round(float(x), 7) for x in vec],
                "logits": [round(float(x), 7) for x in logits],
                "harmonyType": HARMONY_LABELS[int(np.argmax(probs))],
                "colorScore": round(score, 6),
            }
        )
    return out


def main() -> None:
    repo_root = Path(__file__).resolve().parents[2]
    assets_dir = repo_root / "app" / "src" / "main" / "assets"
    kotlin_weights_path = (
        repo_root
        / "app"
        / "src"
        / "main"
        / "java"
        / "com"
        / "zuhdiarifazizi"
        / "projectakhirpapb"
        / "data"
        / "harmony"
        / "ProjectHarmonyWeights.kt"
    )

    X, y_cls, y_score = build_dataset()
    params, val_acc, val_mae = train_mlp(X, y_cls, y_score)
    print(f"Project Harmony validation accuracy: {val_acc * 100:.2f}%, score MAE: {val_mae:.4f}")
    if val_acc < 0.96 or val_mae > 0.025:
        raise RuntimeError(f"Training target not met (acc={val_acc}, mae={val_mae})")

    tflite_bytes = build_tflite_model_bytes(params)
    assets_dir.mkdir(parents=True, exist_ok=True)
    tflite_path = assets_dir / "project_harmony.tflite"
    tflite_path.write_bytes(tflite_bytes)

    golden = build_golden_vectors(params)
    metadata = {
        "modelName": "project_harmony_mlp",
        "version": 1,
        "architecture": [INPUT_SIZE, HIDDEN_1, HIDDEN_2, OUTPUT_SIZE],
        "validationAccuracy": round(val_acc, 4),
        "validationScoreMae": round(val_mae, 5),
        "harmonyLabels": HARMONY_LABELS,
        "goldenVectors": golden,
    }
    (assets_dir / "project_harmony_metadata.json").write_text(
        json.dumps(metadata, indent=2), encoding="utf-8"
    )
    generate_kotlin_weights(params, kotlin_weights_path)
    print(f"Wrote {tflite_path} ({len(tflite_bytes)} bytes) and {kotlin_weights_path}")


if __name__ == "__main__":
    main()
