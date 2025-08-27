#!/usr/bin/env python3
"""
Script de gop cac sprite rieng biet thanh texture atlas moi
"""
from PIL import Image
import os
import glob
import math
import json

def calculate_atlas_size(sprites_info):
    """Tinh kich thuoc atlas toi uu"""
    total_area = sum(info['width'] * info['height'] for info in sprites_info.values())
    
    # Them 20% cho padding va waste space
    total_area = int(total_area * 1.2)
    
    # Tim kich thuoc vuong gan nhat (power of 2)
    atlas_size = 2
    while atlas_size * atlas_size < total_area:
        atlas_size *= 2
    
    # Gioi han toi da la 2048x2048
    if atlas_size > 2048:
        atlas_size = 2048
    
    return atlas_size

def pack_sprites_simple(sprites_info, atlas_size):
    """Thuat toan pack sprite don gian (theo hang)"""
    positions = {}
    
    # Sap xep sprite theo chieu cao giam dan
    sorted_sprites = sorted(sprites_info.items(), key=lambda x: x[1]['height'], reverse=True)
    
    current_x = 0
    current_y = 0
    row_height = 0
    
    for sprite_name, info in sorted_sprites:
        width, height = info['width'], info['height']
        
        # Neu sprite khong vua tren dong hien tai, xuong dong moi
        if current_x + width > atlas_size:
            current_x = 0
            current_y += row_height
            row_height = 0
        
        # Neu vuot qua chieu cao atlas
        if current_y + height > atlas_size:
            print(f"CANH BAO: Sprite {sprite_name} khong vua trong atlas {atlas_size}x{atlas_size}")
            continue
        
        positions[sprite_name] = {
            'x': current_x,
            'y': current_y,
            'width': width,
            'height': height
        }
        
        current_x += width
        row_height = max(row_height, height)
    
    return positions

def create_atlas_file(positions, atlas_path, texture_filename, atlas_size):
    """Tao file .atlas"""
    with open(atlas_path, 'w', encoding='utf-8') as f:
        # Header
        f.write(f"\n{texture_filename}\n")
        f.write(f"size: {atlas_size},{atlas_size}\n")
        f.write("format: RGBA8888\n")
        f.write("filter: Nearest,Nearest\n")
        f.write("repeat: none\n")
        
        # Thong tin cac sprite
        for sprite_name, pos in positions.items():
            f.write(f"{sprite_name}\n")
            f.write("  rotate: false\n")
            f.write(f"  xy: {pos['x']}, {pos['y']}\n")
            f.write(f"  size: {pos['width']}, {pos['height']}\n")
            f.write(f"  orig: {pos['width']}, {pos['height']}\n")
            f.write("  offset: 0, 0\n")
            f.write("  index: -1\n")

def pack_sprites(sprites_dir, output_texture, output_atlas):
    """Gop cac sprite thanh texture atlas"""
    print(f"Bat dau pack sprites tu thu muc: {sprites_dir}")
    print(f"Output texture: {output_texture}")
    print(f"Output atlas: {output_atlas}")
    
    # Tim tat ca file PNG
    png_files = glob.glob(os.path.join(sprites_dir, "*.png"))
    
    if not png_files:
        print("Khong tim thay file PNG nao de pack")
        return
    
    print(f"Tim thay {len(png_files)} file PNG")
    
    # Lay thong tin kich thuoc cac sprite
    sprites_info = {}
    sprites_images = {}
    
    for png_file in png_files:
        try:
            sprite_name = os.path.splitext(os.path.basename(png_file))[0]
            img = Image.open(png_file)
            
            sprites_info[sprite_name] = {
                'width': img.width,
                'height': img.height
            }
            sprites_images[sprite_name] = img
            
            print(f"Loaded: {sprite_name} ({img.width}x{img.height})")
            
        except Exception as e:
            print(f"Loi khi load {png_file}: {e}")
    
    if not sprites_info:
        print("Khong co sprite nao de pack")
        return
    
    # Tinh kich thuoc atlas
    atlas_size = calculate_atlas_size(sprites_info)
    print(f"Kich thuoc atlas: {atlas_size}x{atlas_size}")
    
    # Pack sprites
    print("Dang pack sprites...")
    positions = pack_sprites_simple(sprites_info, atlas_size)
    
    if not positions:
        print("Khong the pack duoc sprite nao")
        return
    
    print(f"Da pack thanh cong {len(positions)}/{len(sprites_info)} sprites")
    
    # Tao texture atlas
    print("Tao texture atlas...")
    atlas_image = Image.new('RGBA', (atlas_size, atlas_size), (0, 0, 0, 0))
    
    for sprite_name, pos in positions.items():
        if sprite_name in sprites_images:
            sprite_img = sprites_images[sprite_name]
            atlas_image.paste(sprite_img, (pos['x'], pos['y']))
    
    # Luu texture
    atlas_image.save(output_texture)
    print(f"Da luu texture: {output_texture}")
    
    # Tao file atlas
    texture_filename = os.path.basename(output_texture)
    create_atlas_file(positions, output_atlas, texture_filename, atlas_size)
    print(f"Da luu atlas file: {output_atlas}")
    
    # Dong cac hinh anh
    for img in sprites_images.values():
        img.close()
    
    print("Hoan thanh pack sprites!")

def pack_to_original_format():
    """Pack lai thanh format goc (textures.png + textures.atlas)"""
    pack_sprites(
        sprites_dir="sprites",
        output_texture="../android/assets/textures_new.png",
        output_atlas="../android/assets/textures_new.atlas"
    )

if __name__ == "__main__":
    print("=== PACK SPRITES SCRIPT ===")
    print("1. Pack thanh file moi (textures_new.png)")
    print("2. Pack voi ten file va thu muc tuy chinh")
    
    choice = input("Chon lua chon (1/2): ")
    
    if choice == "1":
        pack_to_original_format()
    elif choice == "2":
        sprites_dir = input("Thu muc chua sprites (mac dinh 'sprites'): ") or "sprites"
        output_texture = input("Ten file texture output (vd: my_atlas.png): ")
        output_atlas = input("Ten file atlas output (vd: my_atlas.atlas): ")
        
        if output_texture and output_atlas:
            pack_sprites(sprites_dir, output_texture, output_atlas)
        else:
            print("Can nhap du ten file texture va atlas")
    else:
        print("Lua chon khong hop le")
