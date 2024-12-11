# **팟피타운 : 걸으며 설장하는 나만의 펫**

![프로젝트 대표 이미지](readmeimgs/mainimg.png)



## 목차
1. [프로그램 소개](#프로그램-소개)
2. [기술 스택 및 사용 API](#기술-스택-및-사용-api)
3. [설치 및 실행 방법](#설치-및-실행-방법)
4. [참고 자료 및 레퍼런스](#참고-자료-및-레퍼런스)
5. [라이센스](#라이센스)

### 1. 프로그램 소개

![프로그램 소개 이미지](readmeimgs/potpy.png)



### 2. 작업 도구 사용 API

#### **프로그래밍 언어 및 환경**
- **Java**
- **Kotlin**
- **XML**
- **JSON**
- **Node.js**
- **FireBase**
- **macOS**
- **Xcode**

#### **사용된 API 및 라이브러리**
1. **[Firebase](https://firebase.google.com/?gad_source=1&gclid=CjwKCAiAjeW6BhBAEiwAdKltMuoPTgufHwrKERMfAqZlXeDPYi3ZA0mIo5OGaSsSzHKUxfRDxfJSXRoCJKcQAvD_BwE&gclsrc=aw.ds&hl=ko)**
   - Cloud Firestore
   - Firebase Realtime Database
   - Firebase Authentication
   - Firebase Storage
2. **[Google API](https://developers.google.com/maps?hl=ko&_gl=1*gug90s*_up*MQ..*_ga*NDA4OTE5MDk4LjE3MzM4OTY0ODA.*_ga_NRWSTWS78N*MTczMzg5NjQ3OS4xLjEuMTczMzg5NjQ4MC4wLjAuMA..)**
   - Google Places API
   - Google Maps API
   - Google Play Services Location
3. **Retrofit**
   - `retrofit2:converter-gson`
   - `retrofit2:converter-simplexml`
4. **Glide**
   - 이미지 로딩 및 처리
5. **[Lottie](https://lottiefiles.com/kr/)**
   - 애니메이션 지원
6. **AndroidX**
   - Material Design
   - ConstraintLayout
   - AppCompat
   - Espresso (테스트)

---

### 3. 설치 및 실행 방법

#### **Android Studio에서 실행**
1. Android Studio에서 프로젝트 열기.
2. [가상 디바이스](https://developer.android.com/studio/run/managing-avds?hl=ko) 생성 및 실행.
   - 권장: Nexus 5 API 34.
3. `Run` 버튼 클릭 후 실행 확인.

#### **APK 설치**
1. 프로젝트 수정 시 [APK 빌드](https://learn2you.tistory.com/82).
2. 기기에서 APK 설치 후 실행.

---

### 4. 참고 자료 및 레퍼런스

#### **안드로이드 관련**
- [스와이프 뷰](https://developer.android.com/guide/navigation/navigation-swipe-view-2?hl=ko)🔗
- [카드 뷰](https://snakehips.tistory.com/108)🔗
- [화면 전환 효과](https://dev-yangkj.tistory.com/5)🔗
- [시스템 바 색상 설정](https://latte-is-horse.tistory.com/288)🔗

#### **날씨 관련**
- [기상청 날씨 API 사용하기](https://hanyeop.tistory.com/388)🔗
- [코틀린 Android Retrofit 활용](https://fre2-dom.tistory.com/429)🔗
  
#### **지도 관련**
- [지도 검색창 추가](https://stackoverflow.com/questions/31136527/add-search-toolbar-over-google-map-like-in-native-android-app)🔗
- [지도 표시](https://eunoia3jy.tistory.com/185)🔗
- [Android용 Places SDK](https://developers.google.com/maps/documentation/places/android-sdk?hl=ko&_gl=1*xattt6*_up*MQ..*_ga*NDA4OTE5MDk4LjE3MzM4OTY0ODA.*_ga_NRWSTWS78N*MTczMzg5NjQ3OS4xLjEuMTczMzg5NjYyNS4wLjAuMA..)🔗

#### **이미지 처리**
- [Glide 사용하기](https://velog.io/@krrong/Android-Glide-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0)🔗

#### **Firebase**
- [Firestore 클라우드 문서](https://firebase.google.com/docs/storage?hl=ko)🔗
- [Node.js + Firebase](https://liveloper-jay.tistory.com/16)🔗

#### **추가**
- ChatGPT (프로젝트 코드와 문서 작성 도움)

---

### 5. 라이센스

#### **Apache License 2.0**
Copyright 2020 Philipp Jahoda

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

---

#### **Lottie Simple License (FL 9.13.21)**
Copyright © 2021 Design Barn Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of the public animation files available for download at the LottieFiles site (“Files”) to download, reproduce, modify, publish, distribute, publicly display, and publicly digitally perform such Files, including for commercial purposes, provided that any display, publication, performance, or distribution of Files must contain (and be subject to) the same terms and conditions of this license. Modifications to Files are deemed derivative works and must also be expressly distributed under the same terms and conditions of this license. You may not purport to impose any additional or different terms or conditions on, or apply any technical measures that restrict exercise of, the rights granted under this license. This license does not include the right to collect or compile Files from LottieFiles to replicate or develop a similar or competing service.

Use of Files without attributing the creator(s) of the Files is permitted under this license, though attribution is strongly encouraged. If attributions are included, such attributions should be visible to the end user.

FILES ARE PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. EXCEPT TO THE EXTENT REQUIRED BY APPLICABLE LAW, IN NO EVENT WILL THE CREATOR(S) OF FILES OR DESIGN BARN, INC. BE LIABLE ON ANY LEGAL THEORY FOR ANY SPECIAL, INCIDENTAL, CONSEQUENTIAL, PUNITIVE, OR EXEMPLARY DAMAGES ARISING OUT OF THIS LICENSE OR THE USE OF SUCH FILES.

---

#### **Google 및 Firebase 관련 서비스**
본 프로젝트는 Firebase 및 Google Maps API를 사용하며, 해당 서비스의 약관에 따라 작동합니다. 아래 링크를 통해 약관 내용을 확인할 수 있습니다:
- [Firebase Terms of Service](https://firebase.google.com/terms?authuser=0)
- [Google Maps Platform Terms of Service](https://cloud.google.com/maps-platform/terms/)
