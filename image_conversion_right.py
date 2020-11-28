#!/usr/bin/env python
# coding: utf-8

# In[7]:


# img - 이미지 파일
def image_conversion_right(img):

    import dlib
    import cv2
    import numpy as np
    import matplotlib.pyplot as plt
    from PIL import Image
    detector = dlib.get_frontal_face_detector() 
    predictor = dlib.shape_predictor('../shape_predictor_68_face_landmarks.dat')

    src_img =img
    img_bgr = cv2.imread(src_img)
    img_show = img_bgr.copy()
    img_rgb = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)
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

    index = ALL

    #ret, img_frame = src_img.read()

    dets = detector(img_rgb, 1)


    for face in dets:

        shape = predictor(img_rgb, face) #얼굴에서 68개 점 찾기

        list_points = []
        #list_hsv = []
        for p in shape.parts():
            list_points.append([p.x, p.y])

        list_points = np.array(list_points)
    
    img_all_right = img_rgb[min(list_points[17:27,[1]])[0]:list_points[8][1],list_points[8][0]:list_points[16][0],:]


    # 오른쪽 기준으로 대칭 만들어주는 부분
    right_symmetry = img_all_right[:,::-1]
    img_right_symmetry =np.concatenate((right_symmetry,img_all_right), axis=1)
    
    # 오른쪽 기준으로 변환된 이미지 저장
    im = Image.fromarray(img_right_symmetry)
    im.save("image_right.jpg")

