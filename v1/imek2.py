#############################################
# Copyright (c) 2017 Inversebit
#
# This code is free under the MIT License.
# Full license text: https://opensource.org/licenses/MIT
#
# IMEK v1. This code will analyze an image and search for
# boxes. Then it'll extract them to separate images.
#
# You can try it with the example image: xs.jpg
#
#############################################

import cv2
import numpy as np

################
# DEFINITIONS
################
def nothing(x):
    pass

lastTr1pos = 255
lastTr2pos = 41
lastTr3pos = 3

kernel = np.ones((7, 7), np.uint8)

imgRoute = '.../imek/xs.jpg'

save = True
#save = False


################
# WINDOW CREATION
################
cv2.namedWindow('orig', cv2.WINDOW_AUTOSIZE)
cv2.moveWindow('orig', 0, 0)
cv2.namedWindow('t1', cv2.WINDOW_AUTOSIZE)
cv2.moveWindow('t1', 550, 0)
cv2.namedWindow('t2', cv2.WINDOW_AUTOSIZE)
cv2.moveWindow('t2', 1100, 0)

cv2.createTrackbar('P1', 'orig', 0, 1000, nothing)
cv2.createTrackbar('P2', 'orig', 0, 1000, nothing)
cv2.createTrackbar('P3', 'orig', 0, 1000, nothing)

################
# DEFAULT VALS
################
cv2.setTrackbarPos('P1', 'orig', lastTr1pos)
cv2.setTrackbarPos('P2', 'orig', lastTr2pos)
cv2.setTrackbarPos('P3', 'orig', lastTr3pos)

################
# IMG LOADING & PREPROC
################
img = cv2.imread(imgRoute)
imgray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
fin = cv2.GaussianBlur(img, (51, 51), 0)
imgblurgray = cv2.cvtColor(fin, cv2.COLOR_BGR2GRAY)


while(1):
    k = cv2.waitKey(1) & 0xFF
    if k == 27:
        break

    ################
    # PARAM READING
    ################
    currentTr1pos = cv2.getTrackbarPos('P1', 'orig')
    currentTr2pos = cv2.getTrackbarPos('P2', 'orig')
    currentTr3pos = cv2.getTrackbarPos('P3', 'orig')

    if currentTr1pos != lastTr1pos or currentTr2pos != lastTr2pos or currentTr3pos != lastTr3pos:
        print("---------------------------------------------")

        lastTr1pos = currentTr1pos
        lastTr2pos = currentTr2pos
        lastTr3pos = currentTr3pos

        if lastTr2pos % 2 == 0:
            lastTr2pos = lastTr2pos + 1
            cv2.setTrackbarPos('P2', 'orig', lastTr2pos)

        ################
        # THRES & CONTOURS
        ################
        print("Recalc thres with {0}, {1}, {2}".format(lastTr1pos, lastTr2pos, lastTr3pos))
        thresh = cv2.adaptiveThreshold(imgblurgray, lastTr1pos, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, lastTr2pos, lastTr3pos)
        thresh = cv2.dilate(thresh, kernel, iterations=4)
        thresh = cv2.erode(thresh, kernel, iterations=1)
        thresh = cv2.medianBlur(thresh, 9)
        print("Recalc contours")
        im2, contours, hierarchy = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        print("Got {0} raw contours".format(len(contours)))

        ################
        # CONTOUR FILTERING
        ################
        for c in contours:
            print("cont area {0}".format(cv2.contourArea(c)))

        contours = list(filter(lambda c: cv2.contourArea(c) > 3000, contours))

        print("Filtered - Got {0} contours".format(len(contours)))

        ################
        # RESULT DRAWING
        ################
        imgCopy = cv2.imread(imgRoute)

        for c in contours:
            x, y, w, h = cv2.boundingRect(c)
            print("Rect is x:{0}, y:{1}, w:{2}, h:{3}".format(x, y, w, h))
            cv2.rectangle(imgCopy, (x, y), (x + w, y + h), (0, 0, 255), 5)

        cv2.drawContours(imgCopy, contours, -1, (0, 255, 0), 3)

        ################
        # SHOW IMGS
        ################
        cv2.imshow('orig', cv2.resize(imgCopy, (500, 500)))
        cv2.imshow('t1', cv2.resize(imgblurgray, (500, 500)))
        cv2.imshow('t2', cv2.resize(thresh, (500, 500)))

        ################
        # RECT EXTRACTION
        ################
        imgCopy = cv2.imread(imgRoute)
        thresh = cv2.adaptiveThreshold(imgblurgray, lastTr1pos, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV,
                                       lastTr2pos, lastTr3pos)
        thresh = cv2.dilate(thresh, kernel, iterations=3)
        thresh = cv2.erode(thresh, kernel, iterations=1)
        thresh = cv2.bitwise_not(thresh)

        if save:
            for idx, c in enumerate(contours):
                x, y, w, h = cv2.boundingRect(c)
                roi = thresh[y:y+h, x:x+w]
                cv2.imwrite('x{0}.png'.format(idx), roi)

################
# CLEANUP
################
cv2.destroyAllWindows()
