# 💰 VMoney - Smart Personal Finance Assistant

**VMoney** คือแอปพลิเคชันบริหารจัดการการเงินส่วนบุคคลบนระบบปฏิบัติการ Android ที่รวมความเรียบง่ายเข้ากับเทคโนโลยีอัจฉริยะ ช่วยให้การบันทึกรายรับ-รายจ่ายไม่ใช่เรื่องยากอีกต่อไป

---

## 🌟 จุดเด่นของโปรเจ็ค (Project Overview)
VMoney ถูกออกแบบมาเพื่อแก้ปัญหาการลืมบันทึกรายการใช้จ่าย โดยใช้ AI เข้ามาช่วยสแกนข้อมูลจากใบเสร็จ (ตอนนี้ยังอ่านลายมือไม่ได้ อาจเอาไปปรับปรุงในอนาคต) และมีระบบตรวจจับการทำธุรกรรมจากธนาคารผ่าน Notification โดยอัตโนมัติ ทำให้ผู้ใช้เห็นภาพรวมการเงินได้แบบ Real-time

## 🚀 ฟีเจอร์หลัก (Key Features)
*   **📸 AI Receipt Scanning (NVIDIA Power)**: สแกนใบเสร็จด้วย NVIDIA AI รองรับภาษาไทย/อังกฤษ และอ่านลายมือภาษาไทยได้แต่ตอนนี้ยังไม่แม่นยำพอ พร้อมแยกรายการสินค้าและราคารวมรายบรรทัด
*   **🔔 Automated Bank Tracking**: ตรวจจับและบันทึกรายรับ-รายจ่ายจากการแจ้งเตือนของแอปธนาคารโดยอัตโนมัติ (Notification Listener)
*   **📂 Smart Categorization**: แยกหมวดหมู่ค่าใช้จ่ายตามสถานที่ (เช่น ร้านปฐม, บ้าน, ชั้นสอง) เพื่อการวิเคราะห์ที่ละเอียดขึ้น
*   **📊 Transaction History**: ดูรายการย้อนหลัง เรียงลำดับจากใหม่ไปเก่า และสรุปยอดรวมรายวัน/รายเดือน
*   **🔒 Local First Storage**: ข้อมูลทั้งหมดถูกเก็บไว้ในอุปกรณ์ของผู้ใช้ผ่าน Room Database 

## 🛠️ เทคโนโลยีและ Library ที่ใช้ (Tech Stack)
*   **UI Frameowrk**: [Jetpack Compose](https://developer.android.com/compose) (Modern Android UI toolkit)
*   **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) (สำหรับการเก็บข้อมูลภายในเครื่อง)
*   **Networking**: [OkHttp](https://square.github.io/okhttp/) (สำหรับเชื่อมต่อ OpenRouter/Gemini API)
*   **JSON Handling**: [org.json](https://github.com/stleary/JSON-java) (สำหรับ Parse ข้อมูลจาก AI)
*   **Image Loading**: [Coil](https://coil-kt.github.io/coil/compose/) (แสดงผลรูปภาพประกอบ)
*   **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (สำหรับจัดการระบบแจ้งเตือน Daily Reminder)
*   **OCR Engine**: [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition) (สำหรับช่วยอ่านข้อความเบื้องต้น)
*   **AI Engine**: [NVIDIA Nemotron 12B](https://openrouter.ai/models/nvidia/nemotron-nano-12b-v2-vl:free) ผ่าน API
*   **Architecture**: ViewModel, LiveData, Flow และ Coroutines

## ⚙️ การตั้งค่าก่อนใช้งาน (Configuration)
เพื่อให้ระบบ AI ทำงานได้ คุณต้องมี API Key จาก [OpenRouter](https://openrouter.ai/)
1. เปิดไฟล์ `local.properties` ใน Root Project
2. เพิ่มบรรทัดดังนี้:
   ```properties
   OPENROUTER_API_KEY=your_actual_api_key_here
   ```

## 📱 ความต้องการของระบบ (System Requirements)
*   Android OS: API Level 26 (Android 8.0) ขึ้นไป
*   การอนุญาตสิทธิ์ (Permissions): กล้อง (Camera), การแจ้งเตือน (Notification Listener Access)

## 🔍 ข้อมูลทางเทคนิคเพิ่มเติม (Technical Details)

### 1. ทำไมต้องใช้ NVIDIA AI ในการสแกนใบเสร็จ?
เราเลือกใช้โมเดล **nvidia/nemotron-nano-12b-v2-vl** (Vision-Language Model) แทน OCR แบบเดิม เพราะ:
*   **Context Awareness**: สามารถแยกแยะความสัมพันธ์ระหว่าง "ชื่อสินค้า" และ "ราคา" ได้อย่างแม่นยำแม้ใน Layout ที่ซับซ้อน
*   **JSON Extraction**: AI สามารถแปลงรูปภาพเป็นข้อมูลโครงสร้าง JSON ได้โดยตรง ทำให้แอปนำไปบันทึกลง Database ได้ทันที
*   **High Precision**: ให้ความแม่นยำสูงในการอ่านตัวเลขทศนิยมและหน่วยเงิน

### 2. ระบบการแจ้งเตือน (Notification System)
แอปมีการใช้ระบบแจ้งเตือน 2 ส่วนหลัก:
*   **Daily Reminder**: แจ้งเตือนเพื่อให้ผู้ใช้ไม่ลืมบันทึกรายจ่ายในแต่ละวัน
    *   *ใช้ภาษาอะไรทำ:* เขียนด้วย Kotlin โดยใช้ **WorkManager** ควบคุมเวลา และ **NotificationManager** ในการแสดงผล
*   **Bank Alert Auto-Tracking**: ระบบอ่านยอดเงินโอนเข้าอัตโนมัติ
    *   *ใช้อะไรทำ:* ใช้ **NotificationListenerService** ของ Android เพื่อขอสิทธิ์เข้าถึงการแจ้งเตือนจากแอปอื่น
    *   *ทำงานยังไง:* เมื่อมีการแจ้งเตือนจากแอปธนาคาร (เช่น K Plus, SCB, Krungthai) แอปจะใช้ **Regex (Regular Expression)** ในการดึงตัวเลขยอดเงินออกมา และบันทึกลงฐานข้อมูล Room Database ให้ทันทีโดยที่ผู้ใช้ไม่ต้องกดพิมพ์เอง

---
## Wireframe
<img width="1115" height="658" alt="image" src="https://github.com/user-attachments/assets/aa739559-9705-47a8-a634-094072210c05" />
<img width="1069" height="697" alt="image" src="https://github.com/user-attachments/assets/770fd312-09de-4bfc-9b87-2854e8de7d55" />

- Figma -> https://www.figma.com/design/vC5e1QpARsBcdz1GT0E3dt/mobile?node-id=0-1&p=f&t=Wnu3t4ElnLpsLkok-0



### 🛠️ การอนุญาตสิทธิ์ (Permissions)
เพื่อให้ฟีเจอร์ทำงานได้ครบถ้วน แอปจะขอสิทธิ์:
1.  **Notification Listener Access**: สำหรับฟีเจอร์อ่านยอดเงินโอนอัตโนมัติ
2.  **Camera**: สำหรับสแกนใบเสร็จด้วย AI
3.  **Internet**: สำหรับส่งรูปภาพไปประมวลผลที่ NVIDIA API (OpenRouter)

