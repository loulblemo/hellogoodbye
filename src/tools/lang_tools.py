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
        'fil': 'fil', # Filipino
        'el': 'el', # Greek
        'sv': 'sv', # Swedish
        'fi': 'fi', # Finnish
        'tr': 'tr', # Turkish
        'nl': 'nl', # Dutch
        'pl': 'pl', # Polish
        'hu': 'hu', # Hungarian
        'sw': 'sw', # Swahili
        'ha': 'ha' # Hausa
    }
    return lang_mapping[lang_key]


# Default target languages; use --langs to override
DEFAULT_LANGS = [
    "en",
    "es",
    "fr",
    "de",
    "it",
    "pt",
    "ru",
    "ja",
    "ko",
    "zh-cn",
    "th",      # Thai
    "vi",      # Vietnamese
    "id",      # Indonesian
    "ms",      # Malay
    "tl",      # Tagalog
    "el",      # Greek
    "sv",      # Swedish
    "fi",      # Finnish
    "ar",      # Arabic
    "tr",      # Turkish
    "hi",      # Hindi
    "nl",      # Dutch
    "pl",      # Polish
    "hu",      # Hungarian
    "sw",      # Swahili
    "ha",      # Hausa
]

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
    "th": "th",      # Thai
    "vi": "vi",      # Vietnamese
    "id": "id",      # Indonesian
    "ms": "ms",      # Malay
    "tl": "tl",      # Tagalog
    "el": "el",      # Greek
    "sv": "sv",      # Swedish
    "fi": "fi",      # Finnish
    "ar": "ar",      # Arabic
    "tr": "tr",      # Turkish
    "hi": "hi",      # Hindi
    "nl": "nl",      # Dutch
    "pl": "pl",      # Polish
    "hu": "hu",      # Hungarian
    "sw": "sw",      # Swahili
    "ha": "ha",      # Hausa
    
}