#!/usr/bin/env python3
"""
Full test version of extract_sprites.py
"""
from PIL import Image
import os
import re

def parse_atlas_file(atlas_path):
    """Parse file .atlas de lay thong tin vi tri sprite"""
    sprites = {}
    
    with open(atlas_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    current_sprite = None
    for line in lines:
        line = line.strip()
        
        # Bo qua dong trong va thong tin header
        if not line or line == 'textures.png' or (line.startswith('size:') and ',' in line and current_sprite is None) or line.startswith('format:') or line.startswith('filter:') or line.startswith('repeat:'):
            continue
            
        # Ten sprite (khong bat dau bang space va khong chua ':')
        if line and not line.startswith(' ') and ':' not in line:
            current_sprite = line
            sprites[current_sprite] = {}
        
        # Thong tin sprite
        elif current_sprite and line.startswith('xy:'):
            coords = line.replace('xy: ', '').split(', ')
            sprites[current_sprite]['x'] = int(coords[0])
            sprites[current_sprite]['y'] = int(coords[1])
        
        elif current_sprite and line.startswith('size:'):
            size = line.replace('size: ', '').split(', ')
            sprites[current_sprite]['width'] = int(size[0])
            sprites[current_sprite]['height'] = int(size[1])
    
    return sprites

def extract_sprites(texture_path, atlas_path, output_dir):
    """Tach cac sprite tu texture atlas"""
    print(f"Bat dau tach sprites...")
    print(f"Texture path: {texture_path}")
    print(f"Atlas path: {atlas_path}")
    print(f"Output dir: {output_dir}")
    
    # Tao thu muc output
    os.makedirs(output_dir, exist_ok=True)
    
    # Load texture chinh
    print("Loading texture...")
    texture = Image.open(texture_path)
    print(f"Texture size: {texture.size}")
    
    # Parse atlas file
    print("Parsing atlas...")
    sprites = parse_atlas_file(atlas_path)
    
    print(f"Tim thay {len(sprites)} sprites trong atlas")
    
    # Tach tung sprite
    extracted_count = 0
    for sprite_name, info in sprites.items():
        if 'x' in info and 'y' in info and 'width' in info and 'height' in info:
            try:
                # Cat sprite tu texture chinh
                x, y = info['x'], info['y']
                width, height = info['width'], info['height']
                
                # PIL su dung (left, top, right, bottom)
                sprite_img = texture.crop((x, y, x + width, y + height))
                
                # Luu sprite
                safe_name = re.sub(r'[^\w\-_.]', '_', sprite_name)
                output_path = os.path.join(output_dir, f"{safe_name}.png")
                sprite_img.save(output_path)
                
                print(f"Da tach: {sprite_name} -> {output_path}")
                extracted_count += 1
                
            except Exception as e:
                print(f"Loi khi tach {sprite_name}: {e}")
        else:
            print(f"Thieu thong tin cho sprite: {sprite_name} - {info}")
    
    print(f"Da tach thanh cong {extracted_count}/{len(sprites)} sprites")

if __name__ == "__main__":
    # Duong dan files (tuong doi tu thu muc goc)
    texture_path = "../android/assets/textures.png"
    atlas_path = "../android/assets/textures.atlas"
    output_dir = "sprites"  # Luu trong thu muc sprites
    
    try:
        extract_sprites(texture_path, atlas_path, output_dir)
        print("Hoan thanh viec tach sprites!")
    except Exception as e:
        print(f"Loi: {e}")
        import traceback
        traceback.print_exc()
