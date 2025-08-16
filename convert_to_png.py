#!/usr/bin/env python3
"""
Convert SVG icon to PNG format
Requires: pip install cairosvg
"""

import os
import sys
from pathlib import Path

def convert_svg_to_png(svg_path, png_path, size=512):
    """Convert SVG to PNG using cairosvg"""
    try:
        import cairosvg
        from cairosvg import svg2png
        
        # Read SVG content
        with open(svg_path, 'r', encoding='utf-8') as f:
            svg_content = f.read()
        
        # Convert to PNG
        svg2png(bytestring=svg_content, 
                write_to=png_path, 
                output_width=size, 
                output_height=size)
        
        print(f"✅ Successfully converted {svg_path} to {png_path} ({size}x{size})")
        return True
        
    except ImportError:
        print("❌ cairosvg not found. Installing...")
        try:
            import subprocess
            subprocess.check_call([sys.executable, "-m", "pip", "install", "cairosvg"])
            print("✅ cairosvg installed successfully!")
            
            # Try conversion again
            return convert_svg_to_png(svg_path, png_path, size)
            
        except subprocess.CalledProcessError:
            print("❌ Failed to install cairosvg")
            return False
    except Exception as e:
        print(f"❌ Error converting SVG to PNG: {e}")
        return False

def main():
    """Main conversion function"""
    # Get current directory
    current_dir = Path(__file__).parent
    svg_file = current_dir / "app_icon.svg"
    
    if not svg_file.exists():
        print(f"❌ SVG file not found: {svg_file}")
        return
    
    # Create output directory
    output_dir = current_dir / "png_icons"
    output_dir.mkdir(exist_ok=True)
    
    # Convert to different sizes
    sizes = [512, 256, 128, 64, 48, 32]
    
    print("🎨 Converting SVG to PNG icons...")
    print(f"📁 Output directory: {output_dir}")
    print()
    
    success_count = 0
    for size in sizes:
        png_file = output_dir / f"app_icon_{size}x{size}.png"
        
        if convert_svg_to_png(svg_file, png_file, size):
            success_count += 1
    
    print()
    if success_count > 0:
        print(f"🎉 Successfully created {success_count} PNG icons!")
        print(f"📱 Icons are ready in: {output_dir}")
        print()
        print("💡 You can now use these PNG files in your Android app:")
        print("   - app_icon_512x512.png → Play Store listing")
        print("   - app_icon_128x128.png → App launcher icon")
        print("   - app_icon_64x64.png → Settings icon")
        print("   - app_icon_48x48.png → Notification icon")
        print("   - app_icon_32x32.png → Small UI elements")
    else:
        print("❌ No PNG icons were created successfully")

if __name__ == "__main__":
    main()
