import os
import re
import subprocess
import time
import signal

import cv2
import numpy as np


class get_pic:
    def __init__(self, path):
        self.path = path
        self.flag = True
        self.tp = 1

    # def remove_pic(self):
    #     for root, dirs, files in os.walk(self.path):
    #         for name in files:
    #             if name.endswith(".jpg"):  # 填写规则
    #                 os.remove(os.path.join(root, name))
    #                 print("Delete File: " + os.path.join(root, name))

    def get_realip(self):
        filename = "ip.swbd"
        # open(filename, "w").write("")
        os.system("ipconfig > {}".format(filename))
        text = open("{}".format(filename)).read()
        print(text)
        try:
            ipv4 = re.findall(r'以太网适配器 以太网:(.*?)默认网关', text, re.S)[0]
            ipv4 = re.findall(r'IPv4 地址 . . . . . . . . . . . . :(.*?)子网掩码', ipv4, re.S)[0].replace(" ", "")
            # print(ipv4)
        except:
            ipv4 = re.findall(r'无线局域网适配器 WLAN:(.*?)默认网关', text, re.S)[0]
            ipv4 = re.findall(r'IPv4 地址 . . . . . . . . . . . . :(.*?)子网掩码', ipv4, re.S)[0].replace(" ", "")
            # print(ipv4)
        os.remove(filename)
        return ipv4

    # 接收图片
    # card{数字}为银行卡, received{数字}为人脸
    def get_img(self, rate=2):
        """
        :param rate: 缩放比例
        :return:
        """
        filename = f'{self.path}received{self.tp}.jpg'
        while self.flag:
            if os.path.exists(filename):
                time.sleep(0.3)
                print(f'get {filename}')
                pic = cv2.imread(filename)
                pic = np.rot90(pic)
                x, y = pic.shape[:2]  # 高宽
                left_b = (y * 0.25, x - x * 0.25)  # (x, y)
                w, h = y * 0.515, x * 0.21
                card = pic[round(left_b[1] - h): round(left_b[1]), round(left_b[0]): round(left_b[0] + w)]
                card = cv2.resize(card, (416, 416))
                pic = cv2.resize(pic, (int(y / rate), int(x / rate)))
                cv2.imwrite(filename, pic)
                cv2.imwrite(f'{self.path}card{self.tp}.jpg', card)
                self.tp += 1
                return

    # 发送银行卡号, 此时安卓将停止发送图片
    def Send_card(self, num='-1'):
        """
        在此处调用模型识别卡号和人脸是否匹配, 若匹配改变num的值为银行卡号
        """





        if num != '-1' and num != '':
            self.pf = subprocess.Popen(f'java Send {num}', stdout=subprocess.PIPE, universal_newlines=True, shell=True)
            self.flag = False
        else:
            pass

    def run(self):

        test = ['', '', '', '转账成功']
        t = 0

        ipv4 = self.get_realip().replace('\n', '')
        print('手机输入该ip: ', ipv4)
        self.p = subprocess.Popen(f'java com {self.path} 4714', stdout=subprocess.PIPE, universal_newlines=True, shell=True)
        while self.flag:
            self.get_img()

            self.Send_card(test[t])
            t += 1
  
        print('End')


path = input('输入图片路径[..]:').replace('\\', '/')
path = path + '/'
# path为图片储存路径
# path = './'
p = get_pic(path)
p.run()

