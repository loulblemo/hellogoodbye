#!/usr/bin/env python3

# Reduce images to a minimal set of colors (up to a maximum)
# - Preserves transparency
# - Works on a file or a directory of images
# - Outputs PNG files with reduced palette

import os
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("Pillow (PIL) is required. Install with: pip install pillow")
    sys.exit(1)


def ensure_dir(path):
    # Create directory if it doesn't exist
    Path(path).mkdir(parents=True, exist_ok=True)


def count_unique_colors_rgb(image):
    # Returns number of unique RGB colors or a large sentinel if too many
    # image must be mode 'RGB'
    try:
        colors = image.getcolors(256 * 256 * 256)
        if colors is None:
            return 256 * 256 * 256 + 1
        return len(colors)
    except Exception:
        return 256 * 256 * 256 + 1


def quantize_image_to_max_colors(img_path, out_path, max_colors=4, method="median", dither="none"):
    # Reduce the number of colors in the image while preserving the alpha channel
    with Image.open(img_path) as im:
        im = im.convert("RGBA")
        alpha = im.getchannel("A")
        rgb = im.convert("RGB")

        # Determine quantization method
        if method == "fast":
            q_method = Image.FASTOCTREE
        elif method == "lib":
            q_method = Image.LIBIMAGEQUANT if hasattr(Image, "LIBIMAGEQUANT") else Image.MEDIANCUT
        else:
            q_method = Image.MEDIANCUT

        # Determine dithering
        if dither == "floyd":
            q_dither = Image.Dither.FLOYDSTEINBERG
        else:
            q_dither = Image.Dither.NONE

        # If already within limit, no need to quantize
        unique_count = count_unique_colors_rgb(rgb)
        if unique_count <= max_colors:
            reduced = rgb
        else:
            reduced = rgb.quantize(colors=max_colors, method=q_method, dither=q_dither)
            reduced = reduced.convert("RGB")

        # Reattach alpha and save as PNG
        reduced = reduced.convert("RGBA")
        reduced.putalpha(alpha)

        ensure_dir(Path(out_path).parent)
        reduced.save(out_path, format="PNG", optimize=True)


def collect_images(input_path):
    # Yield image file paths under input_path (file or directory)
    exts = {".png", ".jpg", ".jpeg", ".webp", ".bmp"}
    p = Path(input_path)
    if p.is_file():
        if p.suffix.lower() in exts:
            yield p
        return
    for ext in exts:
        for file in p.rglob(f"*{ext}"):
            yield file


def main():
    # Simple CLI: reduce_colors.py <input> [--out OUT_DIR] [--max 4] [--method median|fast|lib] [--dither none|floyd] [--skip-existing]
    import argparse

    parser = argparse.ArgumentParser(add_help=True)
    parser.add_argument("input", nargs="?", default=str(Path("HelloGoodbye/app/src/main/assets/travel_icons")), help="Input file or directory of images")
    parser.add_argument("--out", dest="out_dir", default=None, help="Output directory (default: <input>_reduced)")
    parser.add_argument("--max", dest="max_colors", type=int, default=4, help="Maximum number of colors")
    parser.add_argument("--method", dest="method", choices=["median", "fast", "lib"], default="median", help="Quantization method")
    parser.add_argument("--dither", dest="dither", choices=["none", "floyd"], default="none", help="Dithering method")
    parser.add_argument("--skip-existing", dest="skip_existing", action="store_true", help="Skip files that already exist in output")

    args = parser.parse_args()

    input_path = Path(args.input)
    if not input_path.exists():
        print(f"❌ Input not found: {input_path}")
        sys.exit(1)

    if args.out_dir:
        out_dir = Path(args.out_dir)
    else:
        if input_path.is_dir():
            out_dir = Path(str(input_path) + "_reduced")
        else:
            out_dir = input_path.parent / (input_path.stem + "_reduced")

    ensure_dir(out_dir)

    images = list(collect_images(input_path))
    if not images:
        print("No images found to process.")
        return

    print(f"🎨 Reducing colors to at most {args.max_colors} using method={args.method}, dither={args.dither}")
    print(f"📁 Input:  {input_path}")
    print(f"📦 Output: {out_dir}")

    processed = 0
    for src in images:
        rel = src.name if src.parent == input_path else src.relative_to(input_path) if input_path.is_dir() else src.name
        dest = out_dir / Path(rel).with_suffix(".png")
        if args.skip_existing and dest.exists():
            print(f"⏭️  Skipping existing {dest}")
            continue
        try:
            quantize_image_to_max_colors(src, dest, max_colors=args.max_colors, method=args.method, dither=args.dither)
            print(f"✅ {rel} → {dest.name}")
            processed += 1
        except Exception as e:
            print(f"❌ Failed {src}: {e}")

    print(f"\nDone. Processed {processed} file(s).")


if __name__ == "__main__":
    main()


