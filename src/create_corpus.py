
import json
import asyncio
from googletrans import Translator
from tqdm import tqdm
from phonemizer import phonemize
from phonemizer.separator import Separator

# Input words to translate
the_words = [
    "hello",
    "goodbye",
    "please",
    "thank you",
    "excuse me",
    "help",
    "sorry",
    "water",
    "yes",
    "no",
]

# Output file
corpus_file = "corpus.json"

# Target languages (10 total). Keys are Google Translate language codes.
target_langs = [
    "en",      # English
    "es",      # Spanish
    "fr",      # French
    "de",      # German
    "it",      # Italian
    "pt",      # Portuguese
    "ru",      # Russian
    "ja",      # Japanese
    "ko",      # Korean
    "zh-cn",   # Chinese (Simplified)
]

def get_ipa(lang_code, text):
    # Best-effort IPA using epitran when available
    # Replaced by phonemizer/espeak-ng backend for improved quality and coverage
    phonemizer_lang_map = {
        "en": "en-us",
        "es": "es",
        "fr": "fr-fr", 
        "de": "de",
        "it": "it",
        "pt": "pt",
        "ru": "ru",
        "ja": "ja",
        "ko": "ko",
        "zh-cn": "cmn",
    }
    ph_code = phonemizer_lang_map.get(lang_code, lang_code)
    ipa = phonemize(
        text,
        language=ph_code,
        backend="espeak",
        separator=Separator(phone="", word=" "),
        strip=True,
        preserve_punctuation=False,
        with_stress=True,
    )
    return ipa if ipa.strip() else None



async def build_corpus(words, langs):
    translator = Translator(service_urls=["translate.googleapis.com"], timeout=15.0)  # stable endpoint + longer timeout
    results = []
    for w in tqdm(words):
        entry = {"original": w}
        for lang in langs:
            # Retry logic for ConnectTimeout
            max_retries = 3
            retry_count = 0
            while retry_count < max_retries:
                try:
                    t = await translator.translate(w, src="en", dest=lang)
                    text = t.text
                    ipa = get_ipa(lang, text)
                    
                    # Try to get Google Translate pronunciation when available
                    google_pronunciation = None
                    try:
                        # Check if pronunciation is available in the translation object
                        if hasattr(t, 'pronunciation') and t.pronunciation:
                            google_pronunciation = t.pronunciation
                        # Also check for extra_data which might contain pronunciation
                        # elif hasattr(t, 'extra_data') and t.extra_data:
                        #     if 'translation' in t.extra_data and len(t.extra_data['translation']) > 0:
                        #         translation_data = t.extra_data['translation'][0]
                        #         if 'translation' in translation_data and len(translation_data['translation']) > 0:
                        #             # Look for pronunciation in the translation data
                        #             for item in translation_data['translation']:
                        #                 if 'pronunciation' in item:
                        #                     google_pronunciation = item['pronunciation']
                        #                     break
                    except Exception as e:
                        # If we can't get pronunciation, continue without it
                        print(f"Could not get pronunciation for {w} -> {lang}: {e}")
                    
                    entry[lang] = {
                        "word": text,
                        "IPA": ipa,
                        "text": text,
                    }
                    
                    # Add Google Translate pronunciation if available
                    if google_pronunciation:
                        entry[lang]["google_pronunciation"] = google_pronunciation
                    
                    break  # Success, exit retry loop
                except Exception as e:
                    if "ConnectTimeout" in str(e) and retry_count < max_retries - 1:
                        retry_count += 1
                        wait_time = 2 ** retry_count  # Exponential backoff: 2, 4, 8 seconds
                        print(f"ConnectTimeout for {w} -> {lang}, retrying in {wait_time}s (attempt {retry_count}/{max_retries})")
                        await asyncio.sleep(wait_time)
                        continue
                    else:
                        # Either not a ConnectTimeout or max retries reached
                        raise e

        results.append(entry)
    return results

if __name__ == "__main__":
    data = asyncio.run(build_corpus(the_words, target_langs))
    with open(corpus_file, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Wrote {len(data)} entries to {corpus_file}")