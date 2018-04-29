# Android-Handpose
implementation of real time pattern matching in Android camera from school project

## Lib dependencies
- Android 4.11 (Jelly Bean)
- Apache Math Library 2.2
- Eclipse + Android SDK

## Algoithm
### Preprocess
- Skin color Extraction : selection of pixel by 0 < H < 60, 50% < S < 100%, 50% < V < 100%
- Tint: lightening the image by shifting V, then mix with purple
- Gray scale: removing color by setting saturation to zero
- Resize: for better performance, the image is made smaller
### Matching
- Surf : blob detection using DoH on integral image, keypoint matching
- DoH : determinant of Hessian (second order of spatial derivative)

## Namespacing
### namespace: surf
- Surf, Surf key point, Gaussian matrix, Hessian matrix, Laplacian response, Integral Image
### namespace: utility
- Image processing, Data warehouse
### namespace: edge
- Canny edge, Binary Sobel edge
### namespace: thread
- Activity task, Delayed surface

## Reference
### About Surf:
- original paper ftp://ftp.vision.ee.ethz.ch/publications/articles/eth_biwi_00517.pdf
- implementation http://code.google.com/p/jopensurf/
### About Edge:
- Canny edge implementation http://www.tomgibara.com/computer-vision/canny-edge-detector
- Sobel http://en.wikipedia.org/wiki/Sobel_operator
