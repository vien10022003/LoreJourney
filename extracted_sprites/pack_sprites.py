#!/usr/bin/env python3
"""
Script de gop cac sprite rieng biet thanh texture atlas moi
"""
from PIL import Image
import os
import glob
import math
import json
import re

def parse_original_atlas(atlas_path):
    """Đọc file atlas gốc và trích xuất metadata của các sprite"""
    sprite_metadata = {}
    
    if not os.path.exists(atlas_path):
        print(f"CẢNH BÁO: Không tìm thấy file atlas gốc: {atlas_path}")
        return sprite_metadata
    
    print(f"Đang đọc metadata từ atlas gốc: {atlas_path}")
    
    try:
        with open(atlas_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        current_sprite = None
        
        for line in lines:
            original_line = line  # Giữ nguyên dòng gốc
            line = line.strip()   # Strip để xử lý
            
            # Bỏ qua dòng trống và header
            if not line or line.startswith('size:') or line.startswith('format:') or \
               line.startswith('filter:') or line.startswith('repeat:') or line.endswith('.png'):
                continue
            
            # Nếu dòng không bắt đầu bằng space/tab VÀ không chứa dấu ":", đây là tên sprite
            if not original_line.startswith(' ') and not original_line.startswith('\t') and ':' not in line:
                current_sprite = line
                sprite_metadata[current_sprite] = {}
            
            # Đọc các thuộc tính của sprite (bắt đầu bằng space/tab)
            elif current_sprite and (original_line.startswith(' ') or original_line.startswith('\t')):
                if line.startswith('split:'):
                    # Trích xuất split values: "split: 5, 5, 5, 4"
                    split_match = re.search(r'split:\s*([0-9, ]+)', line)
                    if split_match:
                        split_values = [int(x.strip()) for x in split_match.group(1).split(',')]
                        sprite_metadata[current_sprite]['split'] = split_values
                
                elif line.startswith('pad:'):
                    # Trích xuất pad values: "pad: 4, 4, 1, 1"
                    pad_match = re.search(r'pad:\s*([0-9, ]+)', line)
                    if pad_match:
                        pad_values = [int(x.strip()) for x in pad_match.group(1).split(',')]
                        sprite_metadata[current_sprite]['pad'] = pad_values
        
        # Thống kê
        sprites_with_split = len([s for s in sprite_metadata.values() if 'split' in s])
        sprites_with_pad = len([s for s in sprite_metadata.values() if 'pad' in s])
        
        print(f"Đã đọc metadata của {len(sprite_metadata)} sprites:")
        print(f"  - {sprites_with_split} sprites có thuộc tính 'split'")
        print(f"  - {sprites_with_pad} sprites có thuộc tính 'pad'")
        
        return sprite_metadata
        
    except Exception as e:
        print(f"LỖI khi đọc file atlas: {e}")
        return {}

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

def create_atlas_file(positions, atlas_path, texture_filename, atlas_size, sprite_metadata=None):
    """Tao file .atlas với metadata từ atlas gốc"""
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
            
            # Thêm split nếu có trong metadata
            if sprite_metadata and sprite_name in sprite_metadata:
                metadata = sprite_metadata[sprite_name]
                if 'split' in metadata:
                    split_values = ', '.join(map(str, metadata['split']))
                    f.write(f"  split: {split_values}\n")
                if 'pad' in metadata:
                    pad_values = ', '.join(map(str, metadata['pad']))
                    f.write(f"  pad: {pad_values}\n")
            
            f.write(f"  orig: {pos['width']}, {pos['height']}\n")
            f.write("  offset: 0, 0\n")
            f.write("  index: -1\n")

def pack_sprites(sprites_dir, output_texture, output_atlas, original_atlas_path=None):
    """Gop cac sprite thanh texture atlas với metadata từ atlas gốc"""
    print(f"Bat dau pack sprites tu thu muc: {sprites_dir}")
    print(f"Output texture: {output_texture}")
    print(f"Output atlas: {output_atlas}")
    
    # Đọc metadata từ atlas gốc nếu có
    sprite_metadata = {}
    if original_atlas_path:
        sprite_metadata = parse_original_atlas(original_atlas_path)
    
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
            
            # Hiển thị thông tin metadata nếu có
            metadata_info = ""
            if sprite_name in sprite_metadata:
                meta = sprite_metadata[sprite_name]
                if 'split' in meta:
                    metadata_info += f" [split: {meta['split']}]"
                if 'pad' in meta:
                    metadata_info += f" [pad: {meta['pad']}]"
            
            print(f"Loaded: {sprite_name} ({img.width}x{img.height}){metadata_info}")
            
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
    
    # Tao file atlas với metadata
    texture_filename = os.path.basename(output_texture)
    create_atlas_file(positions, output_atlas, texture_filename, atlas_size, sprite_metadata)
    print(f"Da luu atlas file: {output_atlas}")
    
    # Thống kê metadata được giữ lại
    preserved_metadata = 0
    for sprite_name in positions.keys():
        if sprite_name in sprite_metadata and ('split' in sprite_metadata[sprite_name] or 'pad' in sprite_metadata[sprite_name]):
            preserved_metadata += 1
    
    if preserved_metadata > 0:
        print(f"✅ Đã giữ lại metadata cho {preserved_metadata} sprites")
    else:
        print("⚠️  Không có metadata nào được giữ lại")
    
    # Dong cac hinh anh
    for img in sprites_images.values():
        img.close()
    
    print("Hoan thanh pack sprites!")

def pack_to_original_format():
    """Pack lai thanh format goc (textures.png + textures.atlas)"""
    pack_sprites(
        sprites_dir="sprites",
        output_texture="../android/assets/textures_new.png",
        output_atlas="../android/assets/textures_new.atlas",
        original_atlas_path="textures.atlas"  # Đọc metadata từ atlas gốc
    )

def pack_and_replace_original():
    """Pack va thay the truc tiep file goc"""
    import shutil
    import os
    
    # Hỏi có muốn backup hay không
    backup_choice = input("Bạn có muốn backup files gốc trước khi thay thế? (Y/n): ").lower().strip()
    should_backup = backup_choice != 'n'
    
    original_texture = "../android/assets/textures.png"
    original_atlas = "../android/assets/textures.atlas"
    
    # Backup files gốc nếu người dùng muốn
    if should_backup:
        backup_dir = "../texture_backup_auto"
        os.makedirs(backup_dir, exist_ok=True)
        
        if os.path.exists(original_texture):
            shutil.copy2(original_texture, f"{backup_dir}/textures_backup.png")
            print(f"💾 Backup: textures.png -> {backup_dir}/")
        
        if os.path.exists(original_atlas):
            shutil.copy2(original_atlas, f"{backup_dir}/textures_backup.atlas")
            print(f"💾 Backup: textures.atlas -> {backup_dir}/")
    else:
        print("⚠️  Bỏ qua backup - file gốc sẽ bị ghi đè trực tiếp")
    
    # Pack sprites mới với metadata từ atlas gốc
    pack_sprites(
        sprites_dir="sprites",
        output_texture=original_texture,
        output_atlas=original_atlas,
        original_atlas_path="textures.atlas"  # Đọc metadata từ atlas gốc
    )
    
    print("\n✅ Hoàn thành! Đã thay thế file gốc.")
    if should_backup:
        print(f"📁 File backup tại: {backup_dir}/")
    print("🎮 Hãy test game để đảm bảo mọi thứ hoạt động bình thường!")

if __name__ == "__main__":
    print("=== PACK SPRITES SCRIPT ===")
    print("1. Pack thanh file moi (textures_new.png)")
    print("2. Pack va thay the truc tiep file goc")
    print("3. Pack voi ten file va thu muc tuy chinh")
    
    choice = input("Chon lua chon (1/2/3): ")
    
    if choice == "1":
        pack_to_original_format()
    elif choice == "2":
        pack_and_replace_original()
    elif choice == "3":
        sprites_dir = input("Thu muc chua sprites (mac dinh 'sprites'): ") or "sprites"
        output_texture = input("Ten file texture output (vd: my_atlas.png): ")
        output_atlas = input("Ten file atlas output (vd: my_atlas.atlas): ")
        
        # Hỏi có muốn sử dụng metadata từ atlas gốc không
        use_metadata = input("Su dung metadata tu atlas goc? (Y/n): ").lower().strip()
        original_atlas_path = "textures.atlas" if use_metadata != 'n' else None
        
        if output_texture and output_atlas:
            pack_sprites(sprites_dir, output_texture, output_atlas, original_atlas_path)
        else:
            print("Can nhap du ten file texture va atlas")
    else:
        print("Lua chon khong hop le")
