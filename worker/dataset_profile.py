#!/usr/bin/env python3
"""数据集分析脚本（第一阶段 mock 版）。

用法::

    python dataset_profile.py --input data/uploads/1.zip --output data/profiles/1.json

输出 JSON 结构对应技术文档 10.1::

    {
      "fileCount": 1200,
      "totalSize": 104857600,
      "detectedType": "IMAGE",
      "classCount": 6,
      "classes": ["scratch", "missing_hole"],
      "warnings": []
    }

mock 版仅依赖标准库：基于扩展名与（zip）内文件列表给出粗略画像；
任何异常都转成 warnings 返回，不抛出，便于后端实现「分析失败不影响上传」。
"""
import argparse
import json
import os
import sys
import zipfile

IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".gif", ".webp"}
TEXT_EXTS = {".txt", ".json"}


def detect_type_by_name(name):
    ext = os.path.splitext(name)[1].lower()
    if ext in IMAGE_EXTS:
        return "IMAGE"
    if ext in (".csv", ".tsv"):
        return "TABULAR"
    if ext in TEXT_EXTS:
        return "TEXT"
    if ext == ".zip":
        return "ZIP"
    return "OTHER"


def profile_zip(path):
    """统计 zip 内文件数、推断类型，并把顶层/次层目录名当作类别。"""
    file_count = 0
    image_count = 0
    classes = set()
    with zipfile.ZipFile(path) as zf:
        for info in zf.infolist():
            parts = [p for p in info.filename.split("/") if p]
            if info.is_dir():
                if parts:
                    classes.add(parts[-1])
                continue
            file_count += 1
            if os.path.splitext(info.filename)[1].lower() in IMAGE_EXTS:
                image_count += 1
            # 形如 train/scratch/img001.jpg，取倒数第二段作为类别
            if len(parts) >= 2:
                classes.add(parts[-2])
    detected = "IMAGE" if image_count > 0 and image_count >= file_count * 0.5 else "ZIP"
    return file_count, detected, sorted(classes)


def build_profile(input_path):
    warnings = []
    if not os.path.exists(input_path):
        warnings.append("input not found: %s" % input_path)
        return {
            "fileCount": 0,
            "totalSize": 0,
            "detectedType": "OTHER",
            "classCount": 0,
            "classes": [],
            "warnings": warnings,
        }

    total_size = os.path.getsize(input_path)
    ext = os.path.splitext(input_path)[1].lower()

    if ext == ".zip":
        try:
            file_count, detected, classes = profile_zip(input_path)
        except zipfile.BadZipFile:
            warnings.append("invalid zip file")
            file_count, detected, classes = 1, "OTHER", []
    else:
        file_count, detected, classes = 1, detect_type_by_name(input_path), []

    if not classes:
        warnings.append("未能从目录结构推断类别，请确认数据集标签组织方式")

    return {
        "fileCount": file_count,
        "totalSize": total_size,
        "detectedType": detected,
        "classCount": len(classes),
        "classes": classes,
        "warnings": warnings,
    }


def main():
    parser = argparse.ArgumentParser(description="AgentTrainHub dataset profiler (mock)")
    parser.add_argument("--input", required=True, help="数据集文件路径")
    parser.add_argument("--output", help="分析结果输出 JSON 路径；省略则只打印到 stdout")
    args = parser.parse_args()

    profile = build_profile(args.input)
    text = json.dumps(profile, ensure_ascii=False, indent=2)

    if args.output:
        out_dir = os.path.dirname(os.path.abspath(args.output))
        os.makedirs(out_dir, exist_ok=True)
        with open(args.output, "w", encoding="utf-8") as fp:
            fp.write(text)
    print(text)
    return 0


if __name__ == "__main__":
    sys.exit(main())
