import os
import argparse
import sys
from google.cloud import translate_v3 as translate
from google.cloud import texttospeech

here = os.path.dirname(os.path.abspath(__file__))
sys.path.append(os.path.join(here, ".."))

from tools.llms import chat
import json



# --- Configuration (UPDATE THESE VALUES) ---
# Replace 'YOUR_PROJECT_ID' with your actual Google Cloud Project ID
PROJECT_ID = "my-free-personal-instance"
# Location should usually be 'global' for Translation API V3 requests
LOCATION = "global"
# ------------------------------------------

# Voice mapping for optimal TTS quality
# Maps language codes to (BCP-47 code, voice_name) tuples
VOICE_MAPPING = {
    "sw": ("sw-KE", "sw-KE-Chirp3-HD-Achernar"),  # Swahili -> Swahili (Kenya) with female voice
    "ha": None,     # Hausa not supported by Google TTS
    "en": "en-US",  # English -> English (US)
    "es": "es-ES",  # Spanish -> Spanish (Spain)
    "fr": "fr-FR",  # French -> French (France)
    "de": "de-DE",  # German -> German (Germany)
    "it": "it-IT",  # Italian -> Italian (Italy)
    "pt": "pt-PT",  # Portuguese -> Portuguese (Portugal)
    "ru": "ru-RU",  # Russian -> Russian (Russia)
    "ja": "ja-JP",  # Japanese -> Japanese (Japan)
    "ko": "ko-KR",  # Korean -> Korean (South Korea)
    "zh-cn": "zh-CN",  # Chinese Simplified -> Chinese (China)
    "zh-tw": "zh-TW",  # Chinese Traditional -> Chinese (Taiwan)
    "ar": "ar-XA",  # Arabic -> Arabic (Gulf)
    "hi": "hi-IN",  # Hindi -> Hindi (India)
    "th": "th-TH",  # Thai -> Thai (Thailand)
    "vi": "vi-VN",  # Vietnamese -> Vietnamese (Vietnam)
    "id": "id-ID",  # Indonesian -> Indonesian (Indonesia)
    "ms": "ms-MY",  # Malay -> Malay (Malaysia)
    "tl": "fil-PH", # Filipino -> Filipino (Philippines)
    "nl": "nl-NL",  # Dutch -> Dutch (Netherlands)
    "sv": "sv-SE",  # Swedish -> Swedish (Sweden)
    "da": "da-DK",  # Danish -> Danish (Denmark)
    "no": "nb-NO",  # Norwegian -> Norwegian (Norway)
    "fi": "fi-FI",  # Finnish -> Finnish (Finland)
    "pl": "pl-PL",  # Polish -> Polish (Poland)
    "tr": "tr-TR",  # Turkish -> Turkish (Turkey)
    "el": "el-GR",  # Greek -> Greek (Greece)
    "he": "he-IL",  # Hebrew -> Hebrew (Israel)
    "hu": "hu-HU",  # Hungarian -> Hungarian (Hungary)
    "cs": "cs-CZ",  # Czech -> Czech (Czech Republic)
    "sk": "sk-SK",  # Slovak -> Slovak (Slovakia)
    "ro": "ro-RO",  # Romanian -> Romanian (Romania)
    "bg": "bg-BG",  # Bulgarian -> Bulgarian (Bulgaria)
    "hr": "hr-HR",  # Croatian -> Croatian (Croatia)
    "sl": "sl-SI",  # Slovenian -> Slovenian (Slovenia)
    "et": "et-EE",  # Estonian -> Estonian (Estonia)
    "lv": "lv-LV",  # Latvian -> Latvian (Latvia)
    "lt": "lt-LT",  # Lithuanian -> Lithuanian (Lithuania)
    "uk": "uk-UA",  # Ukrainian -> Ukrainian (Ukraine)
    "be": "be-BY",  # Belarusian -> Belarusian (Belarus)
    "ka": "ka-GE",  # Georgian -> Georgian (Georgia)
    "hy": "hy-AM",  # Armenian -> Armenian (Armenia)
    "az": "az-AZ",  # Azerbaijani -> Azerbaijani (Azerbaijan)
    "kk": "kk-KZ",  # Kazakh -> Kazakh (Kazakhstan)
    "ky": "ky-KG",  # Kyrgyz -> Kyrgyz (Kyrgyzstan)
    "uz": "uz-UZ",  # Uzbek -> Uzbek (Uzbekistan)
    "mn": "mn-MN",  # Mongolian -> Mongolian (Mongolia)
    "ne": "ne-NP",  # Nepali -> Nepali (Nepal)
    "si": "si-LK",  # Sinhala -> Sinhala (Sri Lanka)
    "ta": "ta-IN",  # Tamil -> Tamil (India)
    "te": "te-IN",  # Telugu -> Telugu (India)
    "kn": "kn-IN",  # Kannada -> Kannada (India)
    "ml": "ml-IN",  # Malayalam -> Malayalam (India)
    "gu": "gu-IN",  # Gujarati -> Gujarati (India)
    "pa": "pa-IN",  # Punjabi -> Punjabi (India)
    "bn": "bn-IN",  # Bengali -> Bengali (India)
    "or": "or-IN",  # Odia -> Odia (India)
    "as": "as-IN",  # Assamese -> Assamese (India)
    "mr": "mr-IN",  # Marathi -> Marathi (India)
    "ur": "ur-PK",  # Urdu -> Urdu (Pakistan)
    "fa": "fa-IR",  # Persian -> Persian (Iran)
    "ps": "ps-AF",  # Pashto -> Pashto (Afghanistan)
    "sd": "sd-PK",  # Sindhi -> Sindhi (Pakistan)
    "bo": "bo-CN",  # Tibetan -> Tibetan (China)
    "my": "my-MM",  # Burmese -> Burmese (Myanmar)
    "km": "km-KH",  # Khmer -> Khmer (Cambodia)
    "lo": "lo-LA",  # Lao -> Lao (Laos)
    "ka": "ka-GE",  # Georgian -> Georgian (Georgia)
    "am": "am-ET",  # Amharic -> Amharic (Ethiopia)
    "ti": "ti-ET",  # Tigrinya -> Tigrinya (Ethiopia)
    "om": "om-ET",  # Oromo -> Oromo (Ethiopia)
    "so": "so-SO",  # Somali -> Somali (Somalia)
    "yo": "yo-NG",  # Yoruba -> Yoruba (Nigeria)
    "ig": "ig-NG",  # Igbo -> Igbo (Nigeria)
    "zu": "zu-ZA",  # Zulu -> Zulu (South Africa)
    "af": "af-ZA",  # Afrikaans -> Afrikaans (South Africa)
    "xh": "xh-ZA",  # Xhosa -> Xhosa (South Africa)
    "st": "st-ZA",  # Sesotho -> Sesotho (South Africa)
    "tn": "tn-BW",  # Tswana -> Tswana (Botswana)
    "ss": "ss-ZA",  # Swati -> Swati (South Africa)
    "ve": "ve-ZA",  # Venda -> Venda (South Africa)
    "ts": "ts-ZA",  # Tsonga -> Tsonga (South Africa)
    "nr": "nr-ZA",  # Southern Ndebele -> Southern Ndebele (South Africa)
    "nso": "nso-ZA", # Northern Sotho -> Northern Sotho (South Africa)
    "zu": "zu-ZA",  # Zulu -> Zulu (South Africa)
}


def generate_respelling(text: str, target_language_code: str):
    
    system_prompt = """
    You are a multilingual phonetics specialist who rewrites foreign words so English speakers can pronounce them naturally.
    - Always confirm the source language if it is provided; otherwise infer it from the context.
    - Produce a json object with a single field:
    {
        "respelling": <english-like respelling>
    }
    - Use common English syllables and stress markers (e.g., "kah-RAH-o-kay") instead of IPA.
    - Preserve spaces for multi-word phrases and capitalize proper nouns appropriately.
    - If pronunciation is ambiguous, choose the most widely accepted variant.
    """     
    
    user_prompt = f"""
    Text: {text}
    Word language code: {target_language_code}
    """

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]
    response = chat(messages, response_format={"type": "json_object"})
    return json.loads(response)["respelling"]



def process_word_and_generate_audio(project_id: str, text: str, target_language_code: str):
    """
    Performs translation and generates pronunciation audio in a single call.

    Note: As requested, this function contains no internal error handling (try/except)
    for the API calls. It will raise a Google Cloud Python SDK exception if any 
    required API is unavailable or returns an error.
    
    Args:
        project_id: Your Google Cloud project ID.
        text: The English word or phrase to process.
        target_language_code: The target language's ISO 639-1 code (e.g., 'zh').
    
    Returns:
        A dictionary containing the results and the filename of the generated audio.
    """
    # Initialize Clients
    translation_client = translate.TranslationServiceClient()
    tts_client = texttospeech.TextToSpeechClient()
    
    parent = f"projects/{project_id}/locations/{LOCATION}"
    
    print(f"--- Processing '{text}' to {target_language_code}...")

    # 1. Perform Translation (Fails if API call encounters an error)
    translation_response = translation_client.translate_text(
        parent=parent,
        contents=[text],
        target_language_code=target_language_code,
        source_language_code="en"
    )

    if not translation_response.translations:
        raise ValueError("Translation response was empty.")

    translated_text = translation_response.translations[0].translated_text
    print(f"Translation: {translated_text}")

    # 2. Romanization step removed; continuing with translation output only.


    # 3. Synthesize Audio (Fails if API call encounters an error)
    # Create audio_files directory if it doesn't exist
    audio_dir = "audio_files"
    os.makedirs(audio_dir, exist_ok=True)
    
    output_file = os.path.join(audio_dir, f"{target_language_code}_{text.replace(' ', '_')}_audio.mp3")
    
    synthesis_input = texttospeech.SynthesisInput(text=translated_text)
    
    # Check if the language is supported for TTS
    voice_info = VOICE_MAPPING.get(target_language_code)
    if voice_info is None:
        raise ValueError(f"Language '{target_language_code}' is not supported by Google Text-to-Speech API")
    
    # Handle both tuple (BCP-47, voice_name) and string (BCP-47 only) formats
    if isinstance(voice_info, tuple):
        bcp47_code, voice_name = voice_info
        voice = texttospeech.VoiceSelectionParams(
            language_code=bcp47_code,
            name=voice_name,
            ssml_gender=texttospeech.SsmlVoiceGender.NEUTRAL
        )
    else:
        # Fallback for languages that only have BCP-47 code
        voice = texttospeech.VoiceSelectionParams(
            language_code=voice_info,
            ssml_gender=texttospeech.SsmlVoiceGender.NEUTRAL
        )

    audio_config = texttospeech.AudioConfig(
        audio_encoding=texttospeech.AudioEncoding.MP3
    )

    response = tts_client.synthesize_speech(
        input=synthesis_input, voice=voice, audio_config=audio_config
    )

    # Save the audio file
    with open(output_file, "wb") as out:
        out.write(response.audio_content)
        print(f"Audio content successfully saved to {output_file}")



    respelling = generate_respelling(translated_text, target_language_code)
    return {
        "translated_text": translated_text,
        "respelling": respelling,
        "audio": output_file
    }


def list_supported_translation_languages(project_id: str):
    """Lists supported languages for the Translation API."""
    client = translate.TranslationServiceClient()
    parent = f"projects/{project_id}/locations/{LOCATION}"

    response = client.get_supported_languages(parent=parent, display_language_code="en")

    languages_dict = {}
    for language in response.languages:
        languages_dict[language.language_code] = language.display_name
    return languages_dict


def list_supported_tts_voices():
    """Lists supported voices for the Text-to-Speech API."""
    client = texttospeech.TextToSpeechClient()
    response = client.list_voices()

    voices_list = []
    for voice in response.voices:
        voices_list.append({
            "language_codes": list(voice.language_codes),
            "name": voice.name,
            "ssml_gender": texttospeech.SsmlVoiceGender(voice.ssml_gender).name,
            "natural_sample_rate_hertz": voice.natural_sample_rate_hertz,
        })
    return voices_list


def main():
    """Main function to parse arguments and call the core processing function."""
    # Ensure the PROJECT_ID is set correctly
    if PROJECT_ID == "YOUR_PROJECT_ID":
        print("ERROR: Please update the 'PROJECT_ID' variable at the top of the script.")
        print("The script cannot run without a valid Google Cloud Project ID.")
        return

    # Check for authentication credentials
    if not os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"):
        print("WARNING: GOOGLE_APPLICATION_CREDENTIALS environment variable is not set.")
        print("Please ensure your service account is authenticated for the APIs to work.")
        
    # Set up command-line arguments
    parser = argparse.ArgumentParser(description="Translate an English word and generate pronunciation audio using Google Cloud APIs.")
    parser.add_argument("--list-translation-languages", action="store_true", help="List all supported languages for the Translation API.")
    parser.add_argument("--list-tts-voices", action="store_true", help="List all supported voices for the Text-to-Speech API.")
    parser.add_argument("word", type=str, nargs="?", help="The English word or phrase to process.")
    parser.add_argument("target_lang", type=str, nargs="?", help="The target language ISO 639-1 code (e.g., 'zh', 'ja', 'es', 'fr').")
    
    args = parser.parse_args()
    
    if args.list_translation_languages:
        translation_languages = list_supported_translation_languages(PROJECT_ID)
        with open("supported_translation_languages.json", "w") as f:
            json.dump(translation_languages, f, indent=4)
        print("Supported translation languages saved to supported_translation_languages.json")
        return

    if args.list_tts_voices:
        voices_list = list_supported_tts_voices()
        with open("supported_tts_voices.json", "w") as f:
            json.dump(voices_list, f, indent=4)
        print("Supported TTS voices saved to supported_tts_voices.json")
        return

    if not args.word or not args.target_lang:
        parser.error("'word' and 'target_lang' are required unless --list-translation-languages or --list-tts-voices is used.")

    # Call the single unified function
    results = process_word_and_generate_audio(
        project_id=PROJECT_ID,
        text=args.word,
        target_language_code=args.target_lang
    )

    print("\n--- Final Results ---")
    print(f"English Input: {args.word}")
    print(f"Target Language: {args.target_lang}")
    print(f"Translated Text: {results['translated_text']}")
    print(f"Respelling: {results['respelling']}")
    print(f"Audio File: {results['audio']}")


if __name__ == "__main__":
    main()