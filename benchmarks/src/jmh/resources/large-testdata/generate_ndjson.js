const fs = require('fs');
const crypto = require('crypto');

// Helper functions to generate random data
const randomInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const randomChoice = (arr) => arr[randomInt(0, arr.length - 1)];
const randomFloat = (min, max, decimals = 4) =>
  (Math.random() * (max - min) + min).toFixed(decimals);

// Sample data arrays
const firstNames = ['Troy', 'Emma', 'Liam', 'Olivia', 'Noah', 'Ava', 'Ethan', 'Sophia', 'Mason', 'Isabella', 'James', 'Mia', 'Benjamin', 'Charlotte', 'Lucas'];
const lastNames = ['Heaney', 'Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Wilson'];
const middleNames = ['Anderson', 'Michael', 'James', 'Marie', 'Ann', 'Lee', 'Ray', 'Lynn', 'Grace', 'Rose'];
const streets = ['Delphia Port', 'Oak Street', 'Main Avenue', 'Maple Drive', 'Cedar Lane', 'Pine Road', 'Elm Boulevard'];
const cities = ['South Leslychester', 'Springfield', 'Riverside', 'Georgetown', 'Franklin', 'Clinton', 'Madison'];
const states = ['Connecticut', 'California', 'Texas', 'Florida', 'New York', 'Pennsylvania', 'Illinois', 'Ohio'];
const countries = ['Fiji', 'USA', 'Canada', 'UK', 'Australia', 'Germany', 'France', 'Japan'];
const jobTitles = ['Future Factors Coordinator', 'Senior Developer', 'Marketing Manager', 'Data Analyst', 'Product Designer'];
const jobDescriptors = ['Corporate', 'Senior', 'Lead', 'Chief', 'Principal', 'Regional'];
const jobAreas = ['Communications', 'Technology', 'Marketing', 'Operations', 'Finance', 'Sales'];
const jobTypes = ['Assistant', 'Manager', 'Director', 'Executive', 'Coordinator', 'Specialist'];
const companies = ['Wolf, Harris and Bauch', 'Tech Corp', 'Global Industries', 'Innovation Labs', 'Digital Solutions'];
const domains = ['well-documented-supervisor.biz', 'example.com', 'test.org', 'demo.net', 'sample.io'];
const issuers = ['mastercard', 'visa', 'amex', 'discover'];

function generateRandomString(length) {
  return crypto.randomBytes(Math.ceil(length / 2))
    .toString('base64')
    .slice(0, length)
    .replace(/[+/=]/g, (c) => ({'+': 'A', '/': 'B', '=': 'C'}[c]));
}

function generateUUID() {
  return crypto.randomUUID();
}

function generateObjectId() {
  return crypto.randomBytes(12).toString('hex');
}

function generateRecord() {
  const firstName = randomChoice(firstNames);
  const lastName = randomChoice(lastNames);
  const username = `${firstName}-${lastName}`;

  const record = {
    status: randomChoice(['active', 'inactive', 'pending']),
    name: {
      first: firstName,
      middle: randomChoice(middleNames),
      last: lastName
    },
    username: username,
    password: generateRandomString(15),
    emails: [
      `${randomChoice(firstNames)}${randomInt(10, 99)}@example.com`,
      `${randomChoice(firstNames)}${randomInt(10, 99)}@example.com`
    ],
    phoneNumber: `1-${randomInt(100, 999)}-${randomInt(100, 999)}-${randomInt(1000, 9999)} x${randomInt(100, 999)}`,
    location: {
      street: `${randomInt(1000, 9999)} ${randomChoice(streets)}`,
      city: randomChoice(cities),
      state: randomChoice(states),
      country: randomChoice(countries),
      zip: String(randomInt(10000, 99999)),
      coordinates: {
        latitude: parseFloat(randomFloat(-90, 90)),
        longitude: parseFloat(randomFloat(-180, 180))
      }
    },
    website: `https://${randomChoice(['first', 'second', 'third', 'fourth'])}-${randomChoice(['texture', 'pattern', 'design', 'concept'])}.com/`,
    domain: randomChoice(domains),
    job: {
      title: randomChoice(jobTitles),
      descriptor: randomChoice(jobDescriptors),
      area: randomChoice(jobAreas),
      type: randomChoice(jobTypes),
      company: randomChoice(companies)
    },
    creditCard: {
      number: `${randomInt(1000, 9999)}-${randomInt(1000, 9999)}-${randomInt(1000, 9999)}-${randomInt(1000, 9999)}`,
      cvv: String(randomInt(100, 999)).padStart(3, '0'),
      issuer: randomChoice(issuers)
    },
    uuid: generateUUID(),
    objectId: generateObjectId()
  };

  return record;
}

function padToSize(jsonString, targetSize) {
  // Add padding field to reach target size
  const currentSize = Buffer.byteLength(jsonString, 'utf8');
  if (currentSize < targetSize) {
    const obj = JSON.parse(jsonString);
    const paddingNeeded = targetSize - currentSize - 15; // Account for padding field overhead
    obj._padding = 'x'.repeat(Math.max(0, paddingNeeded));
    return JSON.stringify(obj);
  }
  return jsonString;
}

// Generate multiple files
const numFiles = 10;
const targetSizeMB = 100;
const targetSizeBytes = targetSizeMB * 1024 * 1024;
const lineSizeBytes = 1024;
const numLines = Math.floor(targetSizeBytes / lineSizeBytes);

console.log(`Generating ${numFiles} files with ${numLines} lines each (~${lineSizeBytes} bytes per line)...\n`);

function generateFile(fileNumber) {
  return new Promise((resolve, reject) => {
    const filename = `dummy_data_${String(fileNumber).padStart(2, '0')}.ndjson`;
    const writeStream = fs.createWriteStream(filename);
    let totalBytes = 0;

    console.log(`[File ${fileNumber}/${numFiles}] Starting ${filename}...`);

    for (let i = 0; i < numLines; i++) {
      const record = generateRecord();
      let line = JSON.stringify(record);
      line = padToSize(line, lineSizeBytes - 1); // -1 for newline character

      writeStream.write(line + '\n');
      totalBytes += Buffer.byteLength(line + '\n', 'utf8');

      if ((i + 1) % 25000 === 0) {
        console.log(`[File ${fileNumber}/${numFiles}] Progress: ${i + 1}/${numLines} lines (${(totalBytes / 1024 / 1024).toFixed(2)} MB)`);
      }
    }

    writeStream.end();

    writeStream.on('finish', () => {
      console.log(`[File ${fileNumber}/${numFiles}] ✓ ${filename} completed! Size: ${(totalBytes / 1024 / 1024).toFixed(2)} MB\n`);
      resolve({ filename, totalBytes });
    });

    writeStream.on('error', reject);
  });
}

// Generate all files sequentially
(async () => {
  const startTime = Date.now();
  const results = [];

  for (let i = 1; i <= numFiles; i++) {
    const result = await generateFile(i);
    results.push(result);
  }

  const endTime = Date.now();
  const totalSize = results.reduce((sum, r) => sum + r.totalBytes, 0);

  console.log('='.repeat(60));
  console.log('ALL FILES GENERATED SUCCESSFULLY!');
  console.log('='.repeat(60));
  console.log(`Total files: ${numFiles}`);
  console.log(`Total lines: ${numLines * numFiles}`);
  console.log(`Total size: ${(totalSize / 1024 / 1024).toFixed(2)} MB`);
  console.log(`Time taken: ${((endTime - startTime) / 1000).toFixed(2)} seconds`);
  console.log('\nFiles generated:');
  results.forEach((r, i) => {
    console.log(`  ${i + 1}. ${r.filename}`);
  });
})();
