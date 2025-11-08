from pymongo import MongoClient

# Paste your Atlas connection string below
uri = "mongodb+srv://sohamanwane111_db_user:11J%40n2004%40ATLAS@synhackcluster.uv3ig6o.mongodb.net/?appName=SynHackCluster"

try:
    client = MongoClient(uri)
    client.admin.command('ping')
    print("✅ Connected successfully to MongoDB Atlas!")
except Exception as e:
    print("❌ Connection failed:", e)
