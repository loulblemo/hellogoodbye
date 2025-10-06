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
    output_file = f"{target_language_code}_{text.replace(' ', '_')}_audio.mp3"
    
    synthesis_input = texttospeech.SynthesisInput(text=translated_text)
    
    # Text-to-Speech uses BCP-47 codes (e.g., 'zh-CN', 'fr-FR'), but often handles 
    # two-letter codes by selecting a standard voice for that region.
    voice = texttospeech.VoiceSelectionParams(
        language_code=target_language_code,
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
    parser.add_argument("word", type=str, help="The English word or phrase to process.")
    parser.add_argument("target_lang", type=str, help="The target language ISO 639-1 code (e.g., 'zh', 'ja', 'es', 'fr').")
    
    args = parser.parse_args()
    
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