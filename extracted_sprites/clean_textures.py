#!/usr/bin/env python3
"""
Script de xoa cac file sprite da tach ra
"""
import os
import glob

def clean_extracted_sprites(output_dir="."):
    """Xoa tat ca cac file PNG sprite da tach ra"""
    print(f"Bat dau xoa cac sprite da tach trong thu muc: {output_dir}")
    
    # Tim tat ca file PNG trong thu muc
    png_files = glob.glob(os.path.join(output_dir, "*.png"))
    
    if not png_files:
        print("Khong tim thay file PNG nao de xoa")
        return
    
    print(f"Tim thay {len(png_files)} file PNG")
    
    deleted_count = 0
    failed_count = 0
    
    for png_file in png_files:
        try:
            # Chi xoa cac file PNG, bo qua cac file khong phai sprite
            filename = os.path.basename(png_file)
            
            # Bo qua cac file system hoac file quan trong khac
            if filename.startswith('.') or filename in ['icon.png', 'logo.png']:
                print(f"Bo qua file: {filename}")
                continue
                
            os.remove(png_file)
            print(f"Da xoa: {filename}")
            deleted_count += 1
            
        except Exception as e:
            print(f"Loi khi xoa {png_file}: {e}")
            failed_count += 1
    
    print(f"\nKet qua:")
    print(f"- Da xoa thanh cong: {deleted_count} file")
    if failed_count > 0:
        print(f"- Loi khi xoa: {failed_count} file")
    
    print("Hoan thanh viec xoa sprites!")

if __name__ == "__main__":
    print("=== CLEAN TEXTURES SCRIPT ===")
    clean_extracted_sprites("sprites")
        

