from PIL import Image
import os

def trim_alpha_and_add_padding(image, padding=5):
    """
    Trim transparent areas around content and add small padding.
    
    Args:
        image: PIL Image object
        padding: Number of pixels to add as padding around content
    
    Returns:
        PIL Image with trimmed alpha and padding
    """
    # Convert to RGBA if not already
    if image.mode != 'RGBA':
        image = image.convert('RGBA')
    
    # Get the alpha channel
    alpha = image.getchannel('A')
    
    # Find the bounding box of non-transparent pixels
    bbox = alpha.getbbox()
    
    if bbox is None:
        # Image is completely transparent
        return image
    
    # Crop to the bounding box
    cropped = image.crop(bbox)
    
    # Create a new image with padding
    new_width = cropped.width + (padding * 2)
    new_height = cropped.height + (padding * 2)
    
    # Create transparent background
    result = Image.new('RGBA', (new_width, new_height), (0, 0, 0, 0))
    
    # Paste the cropped image in the center
    result.paste(cropped, (padding, padding), cropped)
    
    return result

def split_image_into_squares(input_path, output_dir, grid_size=3, padding=5):
    """
    Split an image into a grid of squares and trim alpha around content.
    
    Args:
        input_path: Path to the input image
        output_dir: Directory to save the split images
        grid_size: Number of rows/columns in the grid (default: 3 for 3x3=9 squares)
        padding: Padding to add around trimmed content (default: 5 pixels)
    """
    # Open the image
    with Image.open(input_path) as img:
        width, height = img.size
        
        # Calculate the size of each square
        square_width = width // grid_size
        square_height = height // grid_size
        
        print(f"Original image size: {width}x{height}")
        print(f"Each square will be: {square_width}x{square_height}")
        print(f"Alpha trimming with {padding}px padding will be applied")
        
        # Create output directory if it doesn't exist
        os.makedirs(output_dir, exist_ok=True)
        
        # Split the image into squares
        for row in range(grid_size):
            for col in range(grid_size):
                # Calculate the coordinates for cropping
                left = col * square_width
                upper = row * square_height
                right = left + square_width
                lower = upper + square_height
                
                # Crop the square
                square = img.crop((left, upper, right, lower))
                
                # Trim alpha and add padding
                trimmed_square = trim_alpha_and_add_padding(square, padding)
                
                # Generate filename
                filename = f"square_{row}_{col}.png"
                output_path = os.path.join(output_dir, filename)
                
                # Save the trimmed square
                trimmed_square.save(output_path)
                print(f"Saved {filename} (trimmed from {square_width}x{square_height} to {trimmed_square.width}x{trimmed_square.height})")
        
        print(f"\nSuccessfully split image into {grid_size * grid_size} trimmed squares!")
        print(f"Output directory: {output_dir}")

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) < 2:
        print("Usage: python3 split_image.py <input_image_path> [padding]")
        print("Example: python3 split_image.py image.png 10")
        sys.exit(1)
    
    # Input image path
    input_image = sys.argv[1]
    
    # Padding (optional, default 5)
    padding = int(sys.argv[2]) if len(sys.argv) > 2 else 5
    
    # Create output directory based on input filename
    base_name = os.path.splitext(os.path.basename(input_image))[0]
    output_directory = f"travel_icons/{base_name}_split_squares"
    
    # Split the image into 3x3 grid (9 squares)
    split_image_into_squares(input_image, output_directory, grid_size=3, padding=padding)
