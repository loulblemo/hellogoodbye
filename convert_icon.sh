#!/bin/bash

echo "🎨 HelloGoodbye App Icon Converter"
echo "=================================="
echo ""

# Check if Python 3 is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed or not in PATH"
    echo "Please install Python 3 and try again"
    exit 1
fi

# Check if SVG file exists
if [ ! -f "app_icon.svg" ]; then
    echo "❌ app_icon.svg not found in current directory"
    echo "Please run this script from the directory containing app_icon.svg"
    exit 1
fi

echo "📱 Converting SVG to PNG icons..."
echo ""

# Install requirements if needed
if ! python3 -c "import cairosvg" &> /dev/null; then
    echo "📦 Installing required dependencies..."
    pip3 install -r icon_requirements.txt
    echo ""
fi

# Run the conversion
python3 convert_to_png.py

echo ""
echo "🎉 Icon conversion complete!"
echo "📁 Check the 'png_icons' folder for your PNG files"
