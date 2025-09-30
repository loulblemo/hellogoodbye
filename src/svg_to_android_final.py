#!/usr/bin/env python3
"""
Final SVG to Android Vector Drawable Converter (Hardcoded Paths)

This script converts the specified SVG file (app_icon/app_icon.svg)
into an Android Vector Drawable XML file (ic_launcher_foreground.xml)
located in the HelloGoodbye/app/src/main/res/drawable directory.

It handles paths, colors, and scaling to ensure the icon is correctly
formatted for Android, using a <group> for safe area scaling.
"""

import xml.etree.ElementTree as ET
import re
import sys
from pathlib import Path

# --- Hardcoded Paths and Constants ---
APP_ROOT = Path(__file__).resolve().parent
SVG_INPUT_PATH = APP_ROOT / "app_icon/app_icon.svg"
DRAWABLE_OUTPUT_DIR = APP_ROOT / "HelloGoodbye/app/src/main/res/drawable"
DRAWABLE_NAME = "ic_launcher_foreground" # This will be ic_launcher_foreground.xml
ANDROID_ICON_SIZE_DP = 108
SAFE_AREA_SCALE = 0.65 # Scale factor for artwork content to fit within adaptive icon's safe zone

# Android XML Namespace
ANDROID_NS = "http://schemas.android.com/apk/res/android"
# Register the Android namespace prefix
ET.register_namespace("android", ANDROID_NS)

# --- Color Conversion Helper ---
NAMED_COLORS = {
    "white": "#FFFFFF",
    "black": "#000000",
    "red": "#FF0000",
    "green": "#00FF00",
    "blue": "#0000FF",
    "yellow": "#FFFF00",
    "cyan": "#00FFFF",
    "magenta": "#FF00FF",
    "transparent": "#00000000" # Explicitly define transparent as RGBA
}

def normalize_color(value, opacity=None):
    """Converts SVG color strings to Android-compatible #AARRGGBB format."""
    if value is None:
        return None
    value = value.strip()

    if value.lower() == "none":
        return "#00000000"
    if value.startswith("url("): # Android Vector Drawables don't support SVG gradients directly via fill
        return None

    alpha = 255
    if opacity is not None:
        try:
            alpha = max(0, min(255, int(float(opacity) * 255)))
        except ValueError:
            pass

    # Hex colors
    if value.startswith("#"):
        hex_val = value[1:]
        if len(hex_val) == 3: # #RGB shorthand
            hex_val = "".join(c * 2 for c in hex_val)
        if len(hex_val) == 6: # #RRGGBB
            return f"#{alpha:02X}{hex_val.upper()}"
        if len(hex_val) == 8: # #AARRGGBB
            # If SVG has alpha, combine with opacity if given
            svg_alpha = int(hex_val[:2], 16)
            combined_alpha = max(0, min(255, int(svg_alpha * (alpha / 255.0))))
            return f"#{combined_alpha:02X}{hex_val[2:].upper()}"
        return f"#{alpha:02X}{hex_val[:6].upper()}" # Fallback for malformed hex

    # RGB/RGBA colors
    if value.startswith("rgb"):
        match = re.match(r"rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)", value)
        if match:
            r, g, b, svg_opacity = match.groups()
            r, g, b = int(r), int(g), int(b)
            
            if svg_opacity is not None:
                svg_alpha = max(0, min(255, int(float(svg_opacity) * 255)))
                combined_alpha = max(0, min(255, int(svg_alpha * (alpha / 255.0))))
            else:
                combined_alpha = alpha
            return f"#{combined_alpha:02X}{r:02X}{g:02X}{b:02X}"

    # Named colors
    if value.lower() in NAMED_COLORS:
        base_hex = NAMED_COLORS[value.lower()].lstrip("#")
        return f"#{alpha:02X}{base_hex.upper()}"

    return None # Unable to convert

# --- SVG Parsing and Conversion Logic ---
def get_svg_viewport(root_element):
    """Extracts viewport dimensions from SVG root element."""
    view_box = root_element.attrib.get("viewBox")
    if view_box:
        parts = [float(p) for p in view_box.replace(",", " ").split() if p]
        if len(parts) == 4:
            return parts[2], parts[3] # Return width and height
    
    # Fallback to width/height attributes if viewBox is missing or invalid
    width_attr = root_element.attrib.get("width", str(ANDROID_ICON_SIZE_DP))
    height_attr = root_element.attrib.get("height", str(ANDROID_ICON_SIZE_DP))
    try:
        return float(width_attr), float(height_attr)
    except ValueError:
        return float(ANDROID_ICON_SIZE_DP), float(ANDROID_ICON_SIZE_DP)

def convert_svg_to_vector_drawable():
    """
    Converts the hardcoded SVG_INPUT_PATH to an Android Vector Drawable
    at the hardcoded DRAWABLE_OUTPUT_DIR/DRAWABLE_NAME.xml.
    """
    try:
        if not SVG_INPUT_PATH.exists():
            print(f"❌ Error: Input SVG not found at {SVG_INPUT_PATH}", file=sys.stderr)
            sys.exit(1)

        tree = ET.parse(SVG_INPUT_PATH)
        root = tree.getroot()

        # Get original SVG viewport dimensions
        viewport_width, viewport_height = get_svg_viewport(root)
        print(f"SVG Viewport: {viewport_width}x{viewport_height}")

        # Create the root <vector> element
        vector_attribs = {
            f"{{{ANDROID_NS}}}width": f"{ANDROID_ICON_SIZE_DP}dp",
            f"{{{ANDROID_NS}}}height": f"{ANDROID_ICON_SIZE_DP}dp",
            f"{{{ANDROID_NS}}}viewportWidth": str(viewport_width),
            f"{{{ANDROID_NS}}}viewportHeight": str(viewport_height),
        }
        vector_element = ET.Element("vector", vector_attribs)

        # --- Create a <group> for scaling and translation ---
        # Calculate translation to center the scaled artwork
        translate_x = (viewport_width - (viewport_width * SAFE_AREA_SCALE)) / 2
        translate_y = (viewport_height - (viewport_height * SAFE_AREA_SCALE)) / 2

        group_attribs = {
            f"{{{ANDROID_NS}}}scaleX": str(SAFE_AREA_SCALE),
            f"{{{ANDROID_NS}}}scaleY": str(SAFE_AREA_SCALE),
            f"{{{ANDROID_NS}}}translateX": str(translate_x),
            f"{{{ANDROID_NS}}}translateY": str(translate_y),
        }
        group_element = ET.Element("group", group_attribs)

        # Iterate through all elements to find paths and append to the <group>
        for element in root.iter():
            if "path" in element.tag.lower(): # Handles elements like {http://www.w3.org/2000/svg}path
                path_data = element.attrib.get("d")
                if not path_data:
                    continue

                # Get attributes from element directly or from style attribute
                style_attrs = {}
                style_string = element.attrib.get("style")
                if style_string:
                    for part in style_string.split(';'):
                        if ':' in part:
                            key, value = part.split(':', 1)
                            style_attrs[key.strip()] = value.strip()

                fill_value = element.attrib.get("fill", style_attrs.get("fill", "#000000"))
                fill_opacity = element.attrib.get("fill-opacity", style_attrs.get("fill-opacity"))
                stroke_value = element.attrib.get("stroke", style_attrs.get("stroke"))
                stroke_opacity = element.attrib.get("stroke-opacity", style_attrs.get("stroke-opacity"))
                stroke_width = element.attrib.get("stroke-width", style_attrs.get("stroke-width"))
                stroke_linecap = element.attrib.get("stroke-linecap", style_attrs.get("stroke-linecap"))
                stroke_linejoin = element.attrib.get("stroke-linejoin", style_attrs.get("stroke-linejoin"))
                
                path_attribs = {f"{{{ANDROID_NS}}}pathData": path_data}

                # Normalize and apply fill color
                final_fill_color = normalize_color(fill_value, fill_opacity)
                if final_fill_color:
                    path_attribs[f"{{{ANDROID_NS}}}fillColor"] = final_fill_color
                
                # Normalize and apply stroke color
                final_stroke_color = normalize_color(stroke_value, stroke_opacity)
                if final_stroke_color:
                    path_attribs[f"{{{ANDROID_NS}}}strokeColor"] = final_stroke_color
                
                # Apply stroke width (this will be scaled by the group transformation)
                if stroke_width and float(stroke_width) > 0:
                    path_attribs[f"{{{ANDROID_NS}}}strokeWidth"] = stroke_width
                
                # Apply line cap/join
                if stroke_linecap:
                    path_attribs[f"{{{ANDROID_NS}}}strokeLineCap"] = stroke_linecap
                if stroke_linejoin:
                    path_attribs[f"{{{ANDROID_NS}}}strokeLineJoin"] = stroke_linejoin

                path_element = ET.Element("path", path_attribs)
                group_element.append(path_element) # Append to group instead of vector_element
        
        vector_element.append(group_element) # Add the group to the vector element

        # Ensure the output directory exists
        DRAWABLE_OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
        output_filepath = DRAWABLE_OUTPUT_DIR / f"{DRAWABLE_NAME}.xml"

        # Write the final XML file
        xml_string = ET.tostring(vector_element, encoding='utf-8').decode('utf-8')
        with open(output_filepath, "w", encoding="utf-8") as f:
            f.write('<?xml version="1.0" encoding="utf-8"?>\n')
            f.write(xml_string)

        print(f"✅ Successfully created Vector Drawable: {output_filepath.relative_to(APP_ROOT)}")
        print(f"Found {len(group_element)} path elements within the group.")

    except Exception as e:
        print(f"❌ An error occurred during conversion: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    convert_svg_to_vector_drawable()
