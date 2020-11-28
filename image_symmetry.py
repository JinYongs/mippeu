#!/usr/bin/env python
# coding: utf-8

# In[ ]:


# 이미지 별로 대칭유사도를 출력해주는 함수
def image_symmetry(image):
    import dlib
    import cv2
    import numpy as np
    import matplotlib.pyplot as plt

    detector = dlib.get_frontal_face_detector()
    predictor = dlib.shape_predictor('../shape_predictor_68_face_landmarks.dat')
    src_img = image
    img_bgr = cv2.imread(src_img)
    img_show = img_bgr.copy()
    img_rgb = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)
    img_rgb2 = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)
    img_hsv = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2HSV)

    # range는 끝값이 포함안됨   
    ALL = list(range(0, 68)) 
    RIGHT_EYEBROW = list(range(17, 22))  
    LEFT_EYEBROW = list(range(22, 27))  
    RIGHT_EYE = list(range(36, 42))  
    LEFT_EYE = list(range(42, 48))  
    NOSE = list(range(27, 36))  
    MOUTH_OUTLINE = list(range(48, 61))  
    MOUTH_INNER = list(range(61, 68)) 
    JAWLINE = list(range(0, 17)) 

    index = ALL#[17,18,19,20,21,22,23,24,25,26]#RIGHT_EYEBROW + LEFT_EYEBROW #ALL#[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,27,30]



    #ret, img_frame = src_img.read()




    dets = detector(img_rgb, 1)


    for face in dets:

        shape = predictor(img_rgb, face) #얼굴에서 68개 점 찾기

        list_points = []
        #list_hsv = []
        for p in shape.parts():
            list_points.append([p.x, p.y])

        list_points = np.array(list_points)
    #list_hsv = np.array(list_points)

        for i,pt in enumerate(list_points[index]):

            pt_pos = (pt[0], pt[1])
        # 이미지에 원하는 좌표 타원으로 그리는 부분
        # 이미지파일, 타원의 중심좌표(x,y), 축의 절반길이, 색상, 선두께
            cv2.circle(img_rgb, pt_pos, 2, (0, 255, 0), -1)

    # 이미지 얼굴 찾는 부분을 시각화 하는 부분! 
   # fig = plt.figure(1)
   # plt.imshow(img_rgb)

   # fig.set_size_inches(15, 15)

   # plt.show()

    def make_total_hsv(num):
        
        if num>=8:
            img_left = img_hsv[min(list_points[17:27,[1]])[0]:max(list_points[17:27,[1]])[0],list_points[0][0]:list_points[8][0],:]
            img_right = img_hsv[min(list_points[17:27,[1]])[0]:max(list_points[17:27,[1]])[0],list_points[8][0]:list_points[16][0],:]
        
        else:        
            img_left = img_hsv[list_points[num][1]:list_points[num+1][1],list_points[num][0]:list_points[8][0],:]
            img_right = img_hsv[list_points[num][1]:list_points[num+1][1],list_points[8][0]:list_points[16-num][0],:]
        
        S_h = [] # 0 ~ 360
        S_s = [] # 0 ~ 100
        S_v = [] # 0 ~ 100
        if len(img_left[0]) > len(img_right[0]):
            height = len(img_right)
            length = len(img_right[0])
        else:
            height = len(img_left)
            length = len(img_left[0])

        for p in range(height): # 높이(세로)
            for q in range(length):  # 길이(가로)
            # error_square = (img_lef[p][q][0] - img_rig[p][-q-1][0~2])**2
                S_h.append((img_left[p][q][0] - img_right[p][-q-1][0])**2)
                S_s.append((img_left[p][q][1] - img_right[p][-q-1][1])**2)
                S_v.append((img_left[p][q][2] - img_right[p][-q-1][2])**2)
        
        S_h = np.sqrt(S_h)
        S_s = np.sqrt(S_s)
        S_v = np.sqrt(S_v)
        # h의 유사도 측정
        total_h = (sum(S_h) / (360 * length * height )) * 100
        # s의 유사도 측정
        total_s = (sum(S_s) / (100 * length * height )) * 100
        # v의 유사도 측정
        total_v = (sum(S_v) / (100 * length * height )) * 100
        # 해당 구간의 전체 hsv 유사도 측정, 값이 작을수록 유사도가 높은 것!
        total_hsv = (total_h + total_s + total_v) / 3
        return total_hsv

    def section_hsv():
        sect_hsv = []
        # 전체 얼굴을 총 9구역으로 나누었기에 총 9구역에서 유사도를 측정한 것
        for i in range(9):
            sect_hsv.append(make_total_hsv(i))
        return sect_hsv

    # face_similarity에는 각 구역별 유사도가 측정되어있음
    face_similarity = section_hsv()
    # 이 list의 average값이 얼굴 전체의 좌우 대칭 유사도 값!
    face_similarity_total = np.average(face_similarity)
    return face_similarity_total

