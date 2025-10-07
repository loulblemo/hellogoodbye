import json
from concurrent.futures import ThreadPoolExecutor, as_completed
from tools.google_api import process_word_and_generate_audio, PROJECT_ID

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


def process_word_language(word, lang):
    """Process a single word for a single language"""
    try:
        # Use the new unified function
        processing_results = process_word_and_generate_audio(
            project_id=PROJECT_ID,
            text=word,
            target_language_code=lang
        )

        return {
            "word": word,
            "lang": lang,
            "result": {
                "word": processing_results["translated_text"],
                "respelling": processing_results["respelling"],
                "audio_file": processing_results["audio"],
            }
        }
    except Exception as e:
        print(f"Error processing '{word}' for language '{lang}': {e}")
        return {
            "word": word,
            "lang": lang,
            "result": {
                "word": None,
                "respelling": None,
                "audio_file": None,
                "error": str(e)
            }
        }


def build_corpus(words, langs, max_workers=10):
    """Build corpus using multi-threading to process all word-language combinations in parallel"""
    # Create all word-language combinations
    tasks = []
    for word in words:
        for lang in langs:
            tasks.append((word, lang))
    
    print(f"Processing {len(tasks)} word-language combinations with {max_workers} workers...")
    
    # Process all combinations in parallel
    results = {}
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        # Submit all tasks
        future_to_task = {
            executor.submit(process_word_language, word, lang): (word, lang) 
            for word, lang in tasks
        }
        
        # Collect results as they complete
        completed = 0
        for future in as_completed(future_to_task):
            word, lang = future_to_task[future]
            try:
                result = future.result()
                word = result["word"]
                lang = result["lang"]
                lang_result = result["result"]
                
                # Initialize word entry if not exists
                if word not in results:
                    results[word] = {"original": word}
                
                # Add language result
                results[word][lang] = lang_result
                
                completed += 1
                print(f"Completed {completed}/{len(tasks)}: '{word}' -> {lang}")
                
            except Exception as e:
                print(f"Unexpected error processing '{word}' for '{lang}': {e}")
                completed += 1
    
    # Convert results dict to list in original order
    final_results = []
    for word in words:
        if word in results:
            final_results.append(results[word])
        else:
            # Fallback if word wasn't processed
            final_results.append({"original": word})
    
    return final_results

if __name__ == "__main__":
    data = build_corpus(the_words, target_langs)
    with open(corpus_file, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Wrote {len(data)} entries to {corpus_file}")