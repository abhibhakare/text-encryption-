<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Text Encryption/Decryption</title>
  <style>
    body {
      font-family: 'Times New Roman', serif;
      background-color: #c3b2b2;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100vh;
    }

    .container {
      background-color: #c3a6a6;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }

    h1 {
      color: #333;
      margin-bottom: 20px;
    }

    label {
      margin-top: 10px;
      font-size: 18px;
      color: #555;
    }

    textarea {
      width: 100%;
      padding: 10px;
      margin: 5px 0;
      border: 1px solid #ccc;
      border-radius: 4px;
      box-sizing: border-box;
    }

    button {
      background-color: #4caf50;
      color: white;
      padding: 10px 15px;
      margin-top: 10px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 16px;
      transition: background-color 0.3s ease;
    }

    button:hover {
      background-color: #45a049;
    }

    h2 {
      margin-top: 20px;
      color: #333;
    }

    p {
      color: #555;
      margin-top: 10px;
      font-size: 18px;
    }

    .error {
      color: #f00;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1>Text Encryption/Decryption</h1>

    <label for="inputText">Enter Text:</label>
    <textarea id="inputText" rows="4" cols="50"></textarea>

    <button onclick="encryptText()">Encrypt</button>
    <button onclick="decryptText()">Decrypt</button>

    <h2>Result:</h2>
    <p id="outputText"></p>
    <p id="errorText" class="error"></p>
  </div>

  <script>
    let encryptionKey;

    async function generateKey() {
      if (!encryptionKey) {
        const storedKey = sessionStorage.getItem('encryptionKey');

        if (storedKey) {
          const buffer = new Uint8Array(atob(storedKey).split('').map(c => c.charCodeAt(0)));
          encryptionKey = await crypto.subtle.importKey('raw', buffer, { name: 'AES-GCM', length: 256 }, true, ['encrypt', 'decrypt']);
        } else {
          encryptionKey = await crypto.subtle.generateKey(
            { name: 'AES-GCM', length: 256 },
            true,
            ['encrypt', 'decrypt']
          );

          const exportedKey = await crypto.subtle.exportKey('raw', encryptionKey);
          const base64Key = btoa(String.fromCharCode(...new Uint8Array(exportedKey)));
          sessionStorage.setItem('encryptionKey', base64Key);
        }
      }

      return encryptionKey;
    }

    async function encryptText() {
      const key = await generateKey(); // Generate or retrieve the key
      const inputText = document.getElementById('inputText').value;
      const encodedText = new TextEncoder().encode(inputText);

      const iv = crypto.getRandomValues(new Uint8Array(12)); // Generate a random IV

      try {
        const encryptedData = await crypto.subtle.encrypt(
          { name: 'AES-GCM', iv: iv },
          key,
          encodedText
        );

        const ivBase64 = btoa(String.fromCharCode(...iv));
        const encryptedDataBase64 = btoa(String.fromCharCode(...new Uint8Array(encryptedData)));

        const encryptedText = ${ivBase64}:${encryptedDataBase64};
        document.getElementById('outputText').innerText = encryptedText;
      } catch (error) {
        console.error('Encryption error:', error);
      }
    }

    async function decryptText() {
      const key = await generateKey(); // Generate or retrieve the key
      const encryptedText = document.getElementById('outputText').innerText;
      const [ivBase64, encryptedDataBase64] = encryptedText.split(':');

      if (!ivBase64 || !encryptedDataBase64) {
        console.error('Invalid encrypted data: No IV or encrypted data.');
        return;
      }

      const iv = new Uint8Array(atob(ivBase64).split('').map(c => c.charCodeAt(0)));
      const encryptedData = new Uint8Array(atob(encryptedDataBase64).split('').map(c => c.charCodeAt(0)));

      try {
        const decryptedData = await crypto.subtle.decrypt(
          { name: 'AES-GCM', iv: iv },
          key,
          encryptedData
        );

        const decryptedText = new TextDecoder().decode(decryptedData);
        document.getElementById('outputText').innerText = decryptedText;
      } catch (error) {
        console.error('Decryption error:', error);
      }
    }
  </script>
</body>
</html>