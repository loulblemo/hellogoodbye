import json
import os
import requests
import time
from googletrans import Translator
from tqdm import tqdm
import urllib.parse

# Initialize Google Translate
translator = Translator()

# Create audio directory if it doesn't exist
audio_dir = "audio_files"
if not os.path.exists(audio_dir):
    os.makedirs(audio_dir)

def get_audio_url(text, target_lang):
    """Get audio URL from Google Translate"""
    try:
        # Get the pronunciation audio URL
        result = translator.translate(text, dest=target_lang)
        
        # Google Translate audio URL format
        # We'll construct the URL manually since googletrans doesn't provide direct audio access
        encoded_text = urllib.parse.quote(text)
        audio_url = f"https://translate.google.com/translate_tts?ie=UTF-8&q={encoded_text}&tl={target_lang}&client=tw-ob"
        
        return audio_url
    except Exception as e:
        print(f"Error getting audio URL for '{text}' in {target_lang}: {e}")
        return None

def download_audio(audio_url, filename):
    """Download audio file from URL"""
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        response = requests.get(audio_url, headers=headers, timeout=10)
        response.raise_for_status()
        
        with open(filename, 'wb') as f:
            f.write(response.content)
        return True
    except Exception as e:
        print(f"Error downloading audio for {filename}: {e}")
        return False

def get_language_code(lang_key):
    """Convert language key to Google Translate language code"""
    lang_mapping = {
        'en': 'en', # English
        'es': 'es', # Spanish
        'fr': 'fr', # French
        'de': 'de', # German
        'it': 'it', # Italian
        'pt': 'pt', # Portuguese
        'ru': 'ru', # Russian
        'ja': 'ja', # Japanese
        'ko': 'ko', # Korean
        'zh-cn': 'zh-cn', # Chinese
        'ar': 'ar', # Arabic
        'hi': 'hi', # Hindi
        'th': 'th', # Thai
        'vi': 'vi', # Vietnamese
        'id': 'id', # Indonesian
        'ms': 'ms', # Malay
        'tl': 'tl', # Tagalog
        'fil': 'fil' # Filipino
    }
    return lang_mapping.get(lang_key, 'en')

def process_corpus():
    """Process the corpus and add audio files"""
    corpus_file = "corpus.json"
    
    # Load corpus
    with open(corpus_file, "r", encoding='utf-8') as f:
        corpus = json.load(f)
    
    print(f"Processing {len(corpus)} entries...")
    
    for i, entry in enumerate(tqdm(corpus, desc="Processing entries")):
        original_word = entry['original']
        
        # Process each language
        for lang_key, lang_data in entry.items():
            if lang_key == 'original':
                continue
                
            if isinstance(lang_data, dict) and 'word' in lang_data:
                word = lang_data['word']
                lang_code = get_language_code(lang_key)
                
                # Create filename for audio
                safe_word = "".join(c for c in word if c.isalnum() or c in (' ', '-', '_')).rstrip()
                safe_word = safe_word.replace(' ', '_')
                audio_filename = f"{original_word}_{lang_key}_{safe_word}.mp3"
                audio_path = os.path.join(audio_dir, audio_filename)
                
                # Check if audio file already exists
                if not os.path.exists(audio_path):
                    # Get audio URL and download
                    audio_url = get_audio_url(word, lang_code)
                    if audio_url:
                        if download_audio(audio_url, audio_path):
                            print(f"Downloaded: {audio_filename}")
                        else:
                            print(f"Failed to download: {audio_filename}")
                    
                    # Add small delay to avoid rate limiting
                    time.sleep(0.5)
                
                # Add audio field to the language data
                lang_data['audio'] = audio_filename
        
        # Save progress every 10 entries
        if (i + 1) % 10 == 0:
            with open(corpus_file, "w", encoding='utf-8') as f:
                json.dump(corpus, f, ensure_ascii=False, indent=2)
            print(f"Saved progress at entry {i + 1}")
    
    # Final save
    with open(corpus_file, "w", encoding='utf-8') as f:
        json.dump(corpus, f, ensure_ascii=False, indent=2)
    
    print("Corpus processing complete!")
    print(f"Audio files saved in: {audio_dir}")

if __name__ == "__main__":
    process_corpus()

