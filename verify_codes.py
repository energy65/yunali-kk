#!/usr/bin/env python3
import hmac
import hashlib

PERMANENT_SECRET = b"ylys_permanent_2024_secret"


def compute_permanent_hmac_fixed(id_part):
    """模拟修复后的 Java 逻辑: hash[i] & 0xff"""
    msg = id_part.encode('utf-8')
    h = hmac.new(PERMANENT_SECRET, msg, hashlib.sha256).digest()
    result = ''
    for i in range(4):
        result += '%02x' % (h[i] & 0xff)
    return result


def compute_permanent_hmac_bug(id_part):
    """模拟修复前的 Java 逻辑(有bug): 负字节符号扩展"""
    msg = id_part.encode('utf-8')
    h = hmac.new(PERMANENT_SECRET, msg, hashlib.sha256).digest()
    result = ''
    for i in range(4):
        b = h[i]
        if b >= 128:
            signed_int = b - 256
            result += '%x' % signed_int
        else:
            result += '%02x' % b
    return result


def main():
    with open('permanent_activation_codes/permanent_codes_0001-0099.txt', 'r', encoding='utf-8') as f:
        lines = [l.strip() for l in f if l.strip().startswith('0')]

    ok_fixed = 0
    ok_bug = 0
    for line in lines:
        parts = line.split('. ', 1)
        if len(parts) < 2:
            continue
        code = parts[1].replace('-', '').replace(' ', '')
        if len(code) != 30:
            continue
        id_part = code[0:8]
        hmac_part = code[8:16]
        expected_fixed = compute_permanent_hmac_fixed(id_part)
        expected_bug = compute_permanent_hmac_bug(id_part)
        if hmac_part == expected_fixed:
            ok_fixed += 1
        if hmac_part == expected_bug:
            ok_bug += 1

    print(f'修复后Java逻辑验证通过: {ok_fixed}/99')
    print(f'修复前Java(bug)逻辑验证通过: {ok_bug}/99')


if __name__ == '__main__':
    main()
