#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
源力影视永久激活码批量生成脚本 v2 (高复杂度版)
格式: XXXXX-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX (30位字符)
字符集: 大写字母、小写字母、数字、特殊字符(!@#$%^&*)

设计:
- [0:8]    ID区: P + 4位随机大写字母填充 + 3位base36编号 = 8字符
             P A A A A B B B   (避免前导0导致的大量重复)
- [8:16]   HMAC签名区: 8位hex，使用完整8字符ID计算
- [16:30]  随机字符区: 14字符，保证4种字符类型齐全、低重复、无连续相同

激活一次后自动作废，不能再次使用
生成数量: 9999 枚
"""

import hmac
import hashlib
import secrets
import os
from datetime import datetime

PERMANENT_SECRET = b"ylys_permanent_2024_secret"
TOTAL_CODES = 99

# 各类字符集
UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
LOWER = "abcdefghijklmnopqrstuvwxyz"
DIGIT = "0123456789"
SPECIAL = "!@#$%^&*"
ALL_RANDOM = UPPER + LOWER + DIGIT + SPECIAL
ID_PAD_CHARS = UPPER  # ID填充区只使用大写字母，确保多样性
ID_NUM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"  # base36

def encode_id(index):
    """生成8位ID: P + 4位随机大写填充 + 3位base36编号"""
    num = index
    num_part = []
    for _ in range(3):
        num_part.append(ID_NUM_CHARS[num % 36])
        num //= 36
    num_str = ''.join(reversed(num_part))
    pad = ''.join(secrets.choice(ID_PAD_CHARS) for _ in range(4))
    return 'P' + pad + num_str

def compute_hmac(id_part):
    """计算HMAC签名 (与Android端一致)"""
    msg = id_part.encode('utf-8')
    h = hmac.new(PERMANENT_SECRET, msg, hashlib.sha256).digest()
    return h[:4].hex()

def generate_random_section(length, has_upper_from_id):
    """生成指定长度的低重复随机字符，保证4种字符类型齐全
    只允许修改此区域，绝不修改ID和HMAC区
    """
    # 每种字符至少多少个
    # 大写: ID区已有5个大写字母(P + 4填充)，如果还需要就补1个
    # 小写: 至少1个
    # 数字: HMAC区有8位hex，含数字和小写字母；为了保险补1个
    # 特殊: 至少1个
    required = {
        'lower': 1,
        'digit': 1,
        'special': 1,
    }
    if not has_upper_from_id:
        required['upper'] = 1

    # 先从每种类型抽取必需字符（每个只用1次，无重复）
    pool = []
    used_chars = set()

    def pick_unique(charset, count=1):
        choices = [c for c in charset if c not in used_chars]
        if len(choices) < count:
            # 如果不够就从全集补
            choices = list(charset)
        selected = secrets.SystemRandom().sample(choices, count)
        used_chars.update(selected)
        pool.extend(selected)

    pick_unique(LOWER, required.get('lower', 0))
    pick_unique(DIGIT, required.get('digit', 0))
    pick_unique(SPECIAL, required.get('special', 0))
    if required.get('upper', 0):
        pick_unique(UPPER, required['upper'])

    # 剩余字符从全集中无重复抽取
    remaining = length - len(pool)
    remaining_choices = [c for c in ALL_RANDOM if c not in used_chars]
    if len(remaining_choices) < remaining:
        # 如果无重复候选不够，扩大范围允许有限重复
        remaining_choices = list(ALL_RANDOM)
    extra = secrets.SystemRandom().sample(remaining_choices, remaining)
    pool.extend(extra)

    # 打乱顺序
    secrets.SystemRandom().shuffle(pool)

    # 确保没有连续两个相同的字符 (最后保险)
    result = list(pool)
    for i in range(1, len(result)):
        if result[i] == result[i - 1]:
            # 找一个不同的替换
            for j in range(len(result)):
                if result[j] != result[i] and (j == 0 or result[j - 1] != result[i]) and (j + 1 == len(result) or result[j + 1] != result[i]):
                    result[i], result[j] = result[j], result[i]
                    break

    return ''.join(result)

def ensure_all_types_in_random(random_section):
    """确认随机区内包含4种字符类型各至少1个（由于ID区已有大写，所以只需要确认3种+检查大写是否存在即可）"""
    has_upper = any(c in UPPER for c in random_section)
    has_lower = any(c in LOWER for c in random_section)
    has_digit = any(c in DIGIT for c in random_section)
    has_special = any(c in SPECIAL for c in random_section)

    section_list = list(random_section)

    # 大写字母检查: 虽然ID区有，但为了全码分布更均匀，随机区也补一个
    if not has_upper:
        candidate = secrets.choice(UPPER)
        for i in range(len(section_list) - 1, -1, -1):
            if section_list[i] not in UPPER and (i == 0 or section_list[i-1] != candidate):
                section_list[i] = candidate
                break

    if not has_lower:
        candidate = secrets.choice(LOWER)
        for i in range(len(section_list) - 1, -1, -1):
            if section_list[i] not in LOWER and (i == 0 or section_list[i-1] != candidate):
                section_list[i] = candidate
                break

    if not has_digit:
        candidate = secrets.choice(DIGIT)
        for i in range(len(section_list) - 1, -1, -1):
            if section_list[i] not in DIGIT and (i == 0 or section_list[i-1] != candidate):
                section_list[i] = candidate
                break

    if not has_special:
        candidate = secrets.choice(SPECIAL)
        for i in range(len(section_list) - 1, -1, -1):
            if section_list[i] not in SPECIAL and (i == 0 or section_list[i-1] != candidate):
                section_list[i] = candidate
                break

    return ''.join(section_list)

def generate_code(index):
    """生成单个永久激活码，绝不触碰ID和HMAC区的字符"""
    # 1. ID区: 8字符 (P + 4随机大写填充 + 3位base36编号)
    id_part = encode_id(index)

    # 2. HMAC签名区: 8位hex
    hmac_part = compute_hmac(id_part)

    # 3. 随机字符区: 14字符 (位置16-29)
    #   由于ID区已经有5个大写字母，has_upper_from_id=True
    random_part = generate_random_section(14, has_upper_from_id=True)

    # 4. 二次检查：确保4种字符类型齐全（只在随机区内修改）
    random_part = ensure_all_types_in_random(random_part)

    # 5. 组合: 8 + 8 + 14 = 30
    combined = id_part + hmac_part + random_part

    # 6. 格式化: XXXXX-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
    return '-'.join(combined[i:i+5] for i in range(0, 30, 5))

def verify_code_struct(formatted_code):
    """调试验证：生成后立即按Java端逻辑验证结构正确性"""
    code = formatted_code.replace('-', '')
    if len(code) != 30:
        return False, "长度错误"
    if code[0] != 'P':
        return False, "首字符不是P"
    id_part = code[0:8]
    hmac_part = code[8:16]
    expected_hmac = compute_hmac(id_part)
    if hmac_part != expected_hmac:
        return False, f"HMAC不匹配: expected={expected_hmac} actual={hmac_part}"
    # 字符类型检查
    has_upper = any(c.isupper() for c in code)
    has_lower = any(c.islower() for c in code)
    has_digit = any(c.isdigit() for c in code)
    has_special = any(c in SPECIAL for c in code)
    if not (has_upper and has_lower and has_digit and has_special):
        return False, f"字符类型不全 U={has_upper} L={has_lower} D={has_digit} S={has_special}"
    return True, "OK"

def main():
    # 输出目录
    output_dir = "permanent_activation_codes"
    os.makedirs(output_dir, exist_ok=True)

    codes = []
    errors = 0
    for i in range(1, TOTAL_CODES + 1):
        code = generate_code(i)
        ok, msg = verify_code_struct(code)
        if not ok:
            errors += 1
            print(f"WARNING: code #{i} 验证失败: {msg}")
        codes.append((i, code))
        if i % 1000 == 0:
            print(f"已生成 {i}/{TOTAL_CODES} 枚...")

    if errors:
        print(f"!! 生成期间 {errors} 枚结构验证失败 !!")

    # 按批次写入文件 (每批1000枚)
    batch_size = 1000
    batch_files = []
    for batch_start in range(0, len(codes), batch_size):
        batch_end = min(batch_start + batch_size, len(codes))
        batch_codes = codes[batch_start:batch_end]
        filename = os.path.join(output_dir, f"permanent_codes_{batch_start+1:04d}-{batch_end:04d}.txt")
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(f"源力影视永久激活码 v2 高复杂度 ({batch_start+1}-{batch_end})\n")
            f.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write(f"激活码类型: 永久激活码 (一次性使用，激活后作废)\n")
            f.write(f"格式: XXXXX-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX\n")
            f.write("=" * 60 + "\n\n")
            for idx, code in batch_codes:
                f.write(f"{idx:04d}. {code}\n")
        batch_files.append(filename)
        print(f"已写入: {filename}")

    # ======= 复杂度统计报告 =======
    positions = list(zip(*[c.replace('-', '') for _, c in codes]))
    from collections import Counter

    print("\n" + "=" * 60)
    print("复杂度统计报告")
    print("=" * 60)

    # ID区多样性
    print("\nID区(位置0-7)唯一字符数:")
    for i in range(8):
        cnt = Counter(positions[i])
        uniq = len(cnt)
        top_char, top_count = cnt.most_common(1)[0]
        top_pct = top_count / len(codes) * 100
        print(f"  位置{i}: 唯一={uniq:>2}  最频繁='{top_char}' {top_count:>4}次 ({top_pct:4.1f}%)")

    # HMAC区
    print("\nHMAC区(位置8-15)唯一字符数:")
    for i in range(8, 16):
        cnt = Counter(positions[i])
        uniq = len(cnt)
        top_char, top_count = cnt.most_common(1)[0]
        top_pct = top_count / len(codes) * 100
        print(f"  位置{i}: 唯一={uniq:>2}  最频繁='{top_char}' {top_count:>4}次 ({top_pct:4.1f}%)")

    # 重复度统计
    dup_counts = []
    consec_doubles = 0
    for _, code in codes:
        raw = code.replace('-', '')
        seen = {}
        dups = 0
        for c in raw:
            seen[c] = seen.get(c, 0) + 1
        for v in seen.values():
            if v > 1:
                dups += (v - 1)
        dup_counts.append(dups)
        for i in range(len(raw) - 1):
            if raw[i] == raw[i + 1]:
                consec_doubles += 1

    avg_dup = sum(dup_counts) / len(dup_counts)
    max_dup = max(dup_counts)
    print(f"\n单码重复字符数: 平均={avg_dup:.2f}  最大={max_dup}")
    print(f"全库连续重复字符总数: {consec_doubles}  (每码约 {consec_doubles/len(codes):.2f})")

    # 生成汇总文件
    summary_path = os.path.join(output_dir, "summary.txt")
    with open(summary_path, 'w', encoding='utf-8') as f:
        f.write("源力影视永久激活码汇总 v2 (高复杂度)\n")
        f.write("=" * 60 + "\n")
        f.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"总数量: {TOTAL_CODES} 枚\n")
        f.write(f"激活码类型: 永久激活码 (一次性使用，激活后作废)\n")
        f.write(f"格式: XXXXX-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX (30字符，6段分节)\n")
        f.write(f"字符集: 大写字母(26) 小写字母(26) 数字(10) 特殊字符(8) 共70种\n")
        f.write(f"区分标识: 首字符 'P' 开头 (与限时激活码区分)\n")
        f.write(f"有效期: 永久有效\n")
        f.write(f"使用规则: 每枚激活码只能使用一次，激活后自动作废\n\n")
        f.write(f"复杂度设计:\n")
        f.write(f"  - [0:8]  ID区: P + 4位随机大写填充 + 3位base36编号 = 8字符\n")
        f.write(f"             (避免旧式前导0导致99%重复的问题)\n")
        f.write(f"  - [8:16] HMAC签名区: 8位hex，基于8位ID使用HMAC-SHA256计算\n")
        f.write(f"  - [16:30]随机区: 14字符，4种字符类型强制齐全、低重复、无连续相同\n")
        f.write(f"             (字符类型填充仅在此区，绝不触碰ID/HMAC)\n\n")
        f.write("批次文件列表:\n")
        for bf in batch_files:
            f.write(f"  - {os.path.basename(bf)}\n")
        f.write(f"\n复杂度统计 (基于全部 {TOTAL_CODES} 枚):\n")
        f.write(f"  - 单码平均重复字符: {avg_dup:.2f}\n")
        f.write(f"  - 单码最高重复字符: {max_dup}\n")
        f.write(f"  - 全库连续重复字符: {consec_doubles} (每码约 {consec_doubles/len(codes):.2f})\n")

    print(f"\n生成完成! 共生成 {TOTAL_CODES} 枚永久激活码")
    print(f"输出目录: {output_dir}/")
    print(f"汇总文件: {summary_path}")

if __name__ == "__main__":
    main()