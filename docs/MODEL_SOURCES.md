# Model download sources

Relay does not bundle models. The first-run screen now includes a download manager
that fetches Moonshine STT/TTS files automatically over the network, so most
users do not need these manual links. They are provided for reference, for
sideloading, and for verification.

> **Note:** The `download.moonshine.ai` CDN rejects bare `HEAD` requests without a
> `User-Agent` header. The Android downloader uses normal `GET` requests and works
> fine; use `curl -A "Mozilla/5.0" -O <url>` if you test from the command line.

## Gemma 4 E2B translation model (`.litertlm`)

Import this file through the app's first-run Storage Access Framework flow.

- **CPU / general Android (recommended)**:  
  `https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true`  
  (~2.59 GB)
- **GPU (if your device has GPU delegate support)**:  
  `https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-gpu.litertlm?download=true`  
  (~2.01 GB)

Repository: https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm

## Moonshine STT models

Relay uses the architecture listed in the table below. All files are downloaded
under `moonshine/stt/<language>/` in the app's private storage.

| Language | Architecture | Files | ~Total |
| --- | --- | --- | --- |
| English (tiny) | 0 | `encoder_model.ort` (13.3 MB)<br>`decoder_model_merged.ort` (30.4 MB)<br>`tokenizer.bin` (0.25 MB) | ~44 MB |
| Spanish (base) | 1 | `encoder_model.ort` (21.0 MB)<br>`decoder_model_merged.ort` (43.6 MB)<br>`tokenizer.bin` (0.24 MB) | ~65 MB |
| Arabic (base) | 1 | `encoder_model.ort` (31.3 MB)<br>`decoder_model_merged.ort` (109 MB)<br>`tokenizer.bin` (0.25 MB) | ~141 MB |
| Japanese (base) | 1 | `encoder_model.ort` (31.3 MB)<br>`decoder_model_merged.ort` (109 MB)<br>`tokenizer.bin` (0.25 MB) | ~141 MB |
| Korean (tiny) | 0 | `encoder_model.ort` (13.2 MB)<br>`decoder_model_merged.ort` (58.3 MB)<br>`tokenizer.bin` (0.25 MB) | ~72 MB |
| Mandarin (base) | 1 | `encoder_model.ort` (31.3 MB)<br>`decoder_model_merged.ort` (109 MB)<br>`tokenizer.bin` (0.25 MB) | ~141 MB |

Base URLs for the full file URLs below:

- English tiny: `https://download.moonshine.ai/model/tiny-en/quantized/tiny-en/`
- Spanish base: `https://download.moonshine.ai/model/base-es/quantized/base-es/`
- Arabic base: `https://download.moonshine.ai/model/base-ar/quantized/base-ar/`
- Japanese base: `https://download.moonshine.ai/model/base-ja/quantized/base-ja/`
- Korean tiny: `https://download.moonshine.ai/model/tiny-ko/quantized/tiny-ko/`
- Mandarin base: `https://download.moonshine.ai/model/base-zh/quantized/base-zh/`

## Moonshine TTS models

TTS assets are downloaded under `moonshine/tts/<language>/` in the app's private
storage. Kokoro voices are used for English, Spanish, Japanese, and Mandarin.
Arabic and Korean fall back to Piper voices because the moonshine-voice 0.1.1 AAR
does not ship Kokoro voices for those languages.

### English (`en`) — Kokoro `af_heart`

Files under `moonshine/tts/en/`:

- `https://download.moonshine.ai/tts/en_us/dict_filtered_heteronyms.tsv` (2.9 MB)
- `https://download.moonshine.ai/tts/en_us/g2p-config.json`
- `https://download.moonshine.ai/tts/en_us/oov/model.ort` (22.1 MB)
- `https://download.moonshine.ai/tts/en_us/oov/onnx-config.json`
- `https://download.moonshine.ai/tts/kokoro/model.ort` (92.6 MB)
- `https://download.moonshine.ai/tts/kokoro/config.json`
- `https://download.moonshine.ai/tts/kokoro/voices/af_heart.kokorovoice` (0.5 MB)

### Spanish (`es`) — Kokoro `ef_dora`

Files under `moonshine/tts/es/`:

- `https://download.moonshine.ai/tts/kokoro/model.ort` (92.6 MB)
- `https://download.moonshine.ai/tts/kokoro/config.json`
- `https://download.moonshine.ai/tts/kokoro/voices/ef_dora.kokorovoice` (0.5 MB)

### Japanese (`ja`) — Kokoro `jf_alpha`

Files under `moonshine/tts/ja/`:

- `https://download.moonshine.ai/tts/ja/dict.tsv` (6.4 MB)
- `https://download.moonshine.ai/tts/ja/roberta_japanese_char_luw_upos_onnx/meta.json`
- `https://download.moonshine.ai/tts/ja/roberta_japanese_char_luw_upos_onnx/vocab.txt`
- `https://download.moonshine.ai/tts/ja/roberta_japanese_char_luw_upos_onnx/tokenizer_config.json`
- `https://download.moonshine.ai/tts/ja/roberta_japanese_char_luw_upos_onnx/model.ort` (41.7 MB)
- `https://download.moonshine.ai/tts/kokoro/model.ort` (92.6 MB)
- `https://download.moonshine.ai/tts/kokoro/config.json`
- `https://download.moonshine.ai/tts/kokoro/voices/jf_alpha.kokorovoice` (0.5 MB)

### Mandarin (`zh`) — Kokoro `zf_xiaobei`

Files under `moonshine/tts/zh/`:

- `https://download.moonshine.ai/tts/zh_hans/dict.tsv` (1.3 MB)
- `https://download.moonshine.ai/tts/zh_hans/roberta_chinese_base_upos_onnx/meta.json`
- `https://download.moonshine.ai/tts/zh_hans/roberta_chinese_base_upos_onnx/vocab.txt`
- `https://download.moonshine.ai/tts/zh_hans/roberta_chinese_base_upos_onnx/tokenizer_config.json`
- `https://download.moonshine.ai/tts/zh_hans/roberta_chinese_base_upos_onnx/model.model.ort` (0.7 MB)
- `https://download.moonshine.ai/tts/zh_hans/roberta_chinese_base_upos_onnx/model.weights.ort` (101.7 MB)
- `https://download.moonshine.ai/tts/kokoro/model.ort` (92.6 MB)
- `https://download.moonshine.ai/tts/kokoro/config.json`
- `https://download.moonshine.ai/tts/kokoro/voices/zf_xiaobei.kokorovoice` (0.5 MB)

### Arabic (`ar`) — Piper `ar_JO-kareem-medium`

Files under `moonshine/tts/ar/`:

- `https://download.moonshine.ai/tts/ar_msa/dict.tsv` (1.4 MB)
- `https://download.moonshine.ai/tts/ar_msa/arabertv02_tashkeel_fadel_onnx/meta.json`
- `https://download.moonshine.ai/tts/ar_msa/arabertv02_tashkeel_fadel_onnx/vocab.txt`
- `https://download.moonshine.ai/tts/ar_msa/arabertv02_tashkeel_fadel_onnx/tokenizer_config.json`
- `https://download.moonshine.ai/tts/ar_msa/arabertv02_tashkeel_fadel_onnx/model.model.ort` (0.8 MB)
- `https://download.moonshine.ai/tts/ar_msa/arabertv02_tashkeel_fadel_onnx/model.weights.ort` (134.6 MB)
- `https://download.moonshine.ai/tts/ar_msa/piper-voices/ar_JO-kareem-medium.model.ort` (1.7 MB)
- `https://download.moonshine.ai/tts/ar_msa/piper-voices/ar_JO-kareem-medium.weights.ort` (15.8 MB)
- `https://download.moonshine.ai/tts/ar_msa/piper-voices/ar_JO-kareem-medium.onnx.json`

### Korean (`ko`) — Piper `ko_KR-melotts-medium`

Files under `moonshine/tts/ko/`:

- `https://download.moonshine.ai/tts/ko/dict.tsv` (2.1 MB)
- `https://download.moonshine.ai/tts/ko/piper-voices/ko_KR-melotts-medium.model.ort` (1.7 MB)
- `https://download.moonshine.ai/tts/ko/piper-voices/ko_KR-melotts-medium.weights.ort` (15.7 MB)
- `https://download.moonshine.ai/tts/ko/piper-voices/ko_KR-melotts-medium.onnx.json`

## Pre-staging files on the device

If you want to avoid downloading on the phone, push the Moonshine files into the
app's private storage before launch. The directory layout must match what
`AssetDownloader` creates:

```bash
# Example: English tiny STT + Kokoro TTS
adb shell mkdir -p /sdcard/Android/data/com.schilling3003.relay/files/moonshine/stt/en
adb push tiny-en/encoder_model.ort tiny-en/decoder_model_merged.ort tiny-en/tokenizer.bin \
    /sdcard/Android/data/com.schilling3003.relay/files/moonshine/stt/en/

adb shell mkdir -p /sdcard/Android/data/com.schilling3003.relay/files/moonshine/tts/en
adb push en_us/ kokoro/ /sdcard/Android/data/com.schilling3003.relay/files/moonshine/tts/en/
```

The `.litertlm` Gemma file must still be imported through the app's SAF flow.
