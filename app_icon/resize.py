#!/usr/bin/env python3
"""
Script to resize app icons and create feature graphics for app stores.
- Resizes input image to 512x512 for app icon
- Creates 1024x500 feature graphic by padding square image on the right
"""

import sys
import os
from PIL import Image, ImageOps

def resize_to_icon(input_path, output_path, size=512):
    """Resize image to square icon format"""
    try:
        with Image.open(input_path) as img:
            # Convert to RGBA if not already
            if img.mode != 'RGBA':
                img = img.convert('RGBA')
            
            # Resize to square maintaining aspect ratio
            img_resized = ImageOps.fit(img, (size, size), Image.Resampling.LANCZOS)
            img_resized.save(output_path, 'PNG')
            print(f"✓ Icon created: {output_path} ({size}x{size})")
            return True
    except Exception as e:
        print(f"✗ Error creating icon: {e}")
        return False

def create_feature_graphic(input_path, output_path, target_width=1024, target_height=500):
    """Create feature graphic by padding square image on the right"""
    try:
        with Image.open(input_path) as img:
            # Convert to RGBA if not already
            if img.mode != 'RGBA':
                img = img.convert('RGBA')
            
            # Resize to square first (using the smaller dimension to maintain square)
            square_size = min(target_width, target_height)
            img_square = ImageOps.fit(img, (square_size, square_size), Image.Resampling.LANCZOS)
            
            # Create new image with target dimensions
            feature_img = Image.new('RGBA', (target_width, target_height), (0, 0, 0, 0))
            
            # Calculate position to center vertically and place on the left
            y_offset = (target_height - square_size) // 2
            feature_img.paste(img_square, (0, y_offset), img_square)
            
            feature_img.save(output_path, 'PNG')
            print(f"✓ Feature graphic created: {output_path} ({target_width}x{target_height})")
            return True
    except Exception as e:
        print(f"✗ Error creating feature graphic: {e}")
        return False

def main():
    if len(sys.argv) < 2:
        print("Usage: python resize.py <input_image> [output_icon] [output_feature]")
        print("Example: python resize.py input.png icon_512.png feature_1024x500.png")
        sys.exit(1)
    
    input_path = sys.argv[1]
    
    # Check if input file exists
    if not os.path.exists(input_path):
        print(f"✗ Input file not found: {input_path}")
        sys.exit(1)
    
    # Generate output filenames if not provided
    base_name = os.path.splitext(os.path.basename(input_path))[0]
    input_dir = os.path.dirname(input_path)
    
    if len(sys.argv) >= 3:
        icon_output = sys.argv[2]
    else:
        icon_output = os.path.join(input_dir, f"{base_name}_icon_512.png")
    
    if len(sys.argv) >= 4:
        feature_output = sys.argv[3]
    else:
        feature_output = os.path.join(input_dir, f"{base_name}_feature_1024x500.png")
    
    print(f"Processing: {input_path}")
    print(f"Icon output: {icon_output}")
    print(f"Feature graphic output: {feature_output}")
    print()
    
    # Create outputs
    success1 = resize_to_icon(input_path, icon_output)
    success2 = create_feature_graphic(input_path, feature_output)
    
    if success1 and success2:
        print("\n✓ All images created successfully!")
    else:
        print("\n✗ Some operations failed")
        sys.exit(1)

if __name__ == "__main__":
    main()
