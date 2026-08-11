# Model download sources

Relay does not bundle models. Use these official links to acquire the assets
needed for the English ↔ Spanish vertical slice. The app can also download
Moonshine STT/TTS files automatically on first use when the device has a
network connection.

## Gemma 4 E2B translation model (`.litertlm`)

Use Android's Storage Access Framework (SAF) to import this file from the app's
first-run screen.

- **CPU / general Android (recommended)**:  
  `https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm`  
  (~2.59 GB)
- **GPU (if your device has GPU delegate support)**:  
  `https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-gpu.litertlm`  
  (~2.01 GB)

Repository: https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm

## Moonshine STT models

Relay's `MoonshineSpeechRecognizer` currently uses architecture 0 (tiny) for
English and the default architecture for each other language.

| Language | Base URL | Files | ~Size |
| --- | --- | --- | --- |
| English (tiny) | `https://download.moonshine.ai/model/tiny-en/quantized/tiny-en` | `encoder_model.ort`, `decoder_model_merged.ort`, `tokenizer.bin` | ~44 MB |
| Spanish (base) | `https://download.moonshine.ai/model/base-es/quantized/base-es` | `encoder_model.ort`, `decoder_model_merged.ort`, `tokenizer.bin` | ~65 MB |
| Arabic (base) | `https://download.moonshine.ai/model/base-ar/quantized/base-ar` | `encoder_model.ort`, `decoder_model_merged.ort`, `tokenizer.bin` | ~141 MB |
| Japanese (base) | `https://download.moonshine.ai/model/base-ja/quantized/base-ja` | `encoder_model.ort`, `decoder_model_merged.ort`, `tokenizer.bin` | ~141 MB |
| Korean (tiny) | `https://download.moonshine.ai/model/tiny-ko/quantized/tiny-ko` | `encoder_model.ort`, `decoder_model_merged.ort`, `tokenizer.bin` | ~72 MB |
| Mandarin (base) | `https://download.moonshine.ai/model/base-zh/quantized/base-zh` | `encoder_model.ort`, `decoder_model_merged.ort`, `tokenizer.bin` | ~141 MB |

## Moonshine TTS models

TTS assets live under `https://download.moonshine.ai/tts/`. The Kokoro
`model.ort` and `config.json` are shared across several languages; each language
also has a voice bundle and, for some languages, a G2P/OOV model.

### English (`en`)

- `https://download.moonshine.ai/tts/en_us/dict_filtered_heteronyms.tsv`
- `https://download.moonshine.ai/tts/en_us/g2p-config.json`
- `https://download.moonshine.ai/tts/en_us/oov/model.ort`
- `https://download.moonshine.ai/tts/en_us/oov/onnx-config.json`
- `https://download.moonshine.ai/tts/kokoro/model.ort`
- `https://download.moonshine.ai/tts/kokoro/config.json`
- `https://download.moonshine.ai/tts/kokoro/voices/af_heart.kokorovoice`

### Spanish (`es`)

- `https://download.moonshine.ai/tts/kokoro/model.ort`
- `https://download.moonshine.ai/tts/kokoro/config.json`
- `https://download.moonshine.ai/tts/kokoro/voices/ef_dora.kokorovoice`

## Pre-staging files on the device

If you want to avoid downloading on the phone, you can push the Moonshine files
into the app's private storage before launch:

```bash
# Example for English STT + TTS
adb shell mkdir -p /sdcard/Android/data/com.schilling3003.relay/files/moonshine/stt/en
adb push tiny-en/encoder_model.ort ... /sdcard/Android/data/com.schilling3003.relay/files/moonshine/stt/en/
adb push en_us/ kokoro/ /sdcard/Android/data/com.schilling3003.relay/files/moonshine/tts/
```

The `.litertlm` Gemma file must still be imported through the app's SAF flow.
