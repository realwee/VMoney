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

## 🛠️ เทคโนโลยีที่ใช้ (Tech Stack)
*   **UI**: [Jetpack Compose](https://developer.android.com/compose) (Modern Android UI toolkit)
*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
*   **Concurrency**: [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html)
*   **Networking**: [OkHttp](https://square.github.io/okhttp/) สำหรับเชื่อมต่อ OpenRouter API
*   **AI Engine**: [NVIDIA Nemotron 12B](https://openrouter.ai/models/nvidia/nemotron-nano-12b-v2-vl:free) ผ่าน OpenRouter API
*   **Dependency Injection**: ViewModel & LiveData

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

---
## Wireframe
<img width="1115" height="658" alt="image" src="https://github.com/user-attachments/assets/aa739559-9705-47a8-a634-094072210c05" />
<img width="1069" height="697" alt="image" src="https://github.com/user-attachments/assets/770fd312-09de-4bfc-9b87-2854e8de7d55" />

- Figma -> https://www.figma.com/design/vC5e1QpARsBcdz1GT0E3dt/mobile?node-id=0-1&p=f&t=Wnu3t4ElnLpsLkok-0



