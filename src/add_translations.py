import sys
import os

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from tools.lang_tools import DEFAULT_LANGS
from tools.lang_tools import phonemizer_lang_map


import json
import argparse
import asyncio
import time
from googletrans import Translator
from tqdm import tqdm
from phonemizer import phonemize
from phonemizer.separator import Separator


def get_ipa(lang_code, text):
    # Best-effort IPA using phonemizer/espeak-ng backend

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
    return ipa if ipa and ipa.strip() else None

async def add_translations(corpus, langs, overwrite, sleep_between, retries):
    translator = Translator(service_urls=["translate.googleapis.com"], timeout=15.0)

    for entry in tqdm(corpus, desc="Augmenting entries"):
        original = entry.get("original", "")
        if not original:
            continue

        for lang in langs:
            if not overwrite and lang in entry:
                continue

            retry_count = 0
            while retry_count < retries:
                try:
                    t = await translator.translate(original, src="en", dest=lang)
                    text = t.text
                    ipa = None
                    try:
                        ipa = get_ipa(lang, text)
                    except Exception as e:
                        # Fallback to None if phonemizer fails
                        ipa = None

                    google_pronunciation = None
                    try:
                        if hasattr(t, "pronunciation") and t.pronunciation:
                            google_pronunciation = t.pronunciation
                    except Exception:
                        google_pronunciation = None

                    entry[lang] = {
                        "word": text,
                        "IPA": ipa,
                        "text": text,
                    }
                    if google_pronunciation:
                        entry[lang]["google_pronunciation"] = google_pronunciation

                    if sleep_between > 0:
                        await asyncio.sleep(sleep_between)
                    break
                except Exception as e:
                    retry_count += 1
                    if retry_count < retries:
                        wait_time = 2 ** retry_count
                        await asyncio.sleep(wait_time)
                    else:
                        raise e

    return corpus

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", default="HelloGoodbye/app/src/main/assets/corpus.json")
    parser.add_argument("--output", default="HelloGoodbye/app/src/main/assets/corpus.json")
    parser.add_argument("--langs", default=",".join(DEFAULT_LANGS))
    parser.add_argument("--overwrite", action="store_true")
    parser.add_argument("--sleep", type=float, default=0.0)
    parser.add_argument("--retries", type=int, default=3)
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_args()

    with open(args.input, "r", encoding="utf-8") as f:
        corpus = json.load(f)

    langs = [l.strip() for l in args.langs.split(",") if l.strip()]

    updated = asyncio.run(
        add_translations(
            corpus=corpus,
            langs=langs,
            overwrite=args.overwrite,
            sleep_between=args.sleep,
            retries=args.retries,
        )
    )

    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(updated, f, ensure_ascii=False, indent=2)

    print("Wrote", len(updated), "entries to", args.output)


