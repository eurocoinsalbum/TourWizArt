from html.parser import HTMLParser
import codecs
import os
from shutil import copyfile
from pathlib import Path
import uuid

START_TITLE_ID = 1708

class StoneInfo():

	def __init__(self):
		self.name = ""
		self.image = ""

		
class MyHTMLParser(HTMLParser):
	
	def __init__(self):
		super().__init__()
		self.row = 0
		self.col = -1
		self.parseName = False
		self.stoneInfos = []
	
	
	def handle_starttag(self, tag, attrs):
		if tag == "tr":
			self.stoneInfos.append(StoneInfo())
			self.col = -1
			return
		if tag == "td":
			self.col += 1
			return
		if tag == "b" and self.col == 3:
			self.parseName = True
		if tag == "img" and self.col == 4:
			for attr in attrs:
				if attr[0] == "src":
					self.stoneInfos[self.row].image = attr[1].split("/")[-1:][0]

			
	def handle_endtag(self, tag):
		if tag == "tr":
			self.row += 1
			self.stoneName = ""
			return
		if tag == "b":
			self.parseName = False
			return

			
	def handle_data(self, data):
		if self.parseName:
			if self.stoneInfos[self.row].name != "":
				self.stoneInfos[self.row].name += " "
			self.stoneInfos[self.row].name += data

			
def prepareImageFilename(stoneInfoImage):
	pos = stoneInfoImage.find("%")
	while pos != -1:
		stoneInfoImage = stoneInfoImage[:pos] + stoneInfoImage[pos + 3:]
		pos = stoneInfoImage.find("%", pos)
	return stoneInfoImage
	
	
if not (Path("images/300pxUUID")).is_dir():
	os.makedirs("images/300pxUUID")

parser = MyHTMLParser()

with codecs.open('stone-list.html', "r", "utf-8") as htmlFile:
	with codecs.open('TourWizArt_insert_datasetObjects.sql', "w", "utf-8") as sqlfileObjects:
		with codecs.open('TourWizArt_insert_titles.sql', "w", "utf-8") as sqlfileTitles:
			parser.feed(htmlFile.read())
			stoneFiles = os.listdir("Liste der Stolpersteine in Mainz – Wikipedia-Dateien")

			index = 0
			for stoneInfo in parser.stoneInfos:
				index += 1
				found = False
				stoneInfoImageFilename = prepareImageFilename(stoneInfo.image)
				for stoneFile in stoneFiles:
					nextUuid = str(uuid.uuid4())
					if stoneInfoImageFilename == stoneFile:
						found = True
						break

				if found:
					sqlfileObjects.write("INSERT INTO dataset_object (tourDatasetUuid, datasetObjectUuid, titleTextId, location, status, artistUuid, styleTextId, year) VALUES ('693fda6d-d998-4807-8347-4f083a10bad7', '" + nextUuid + "', " + str(START_TITLE_ID + index - 1) + ", NULL, 'ACTIVE', 'c47f79dc-ad57-4d97-85ca-65b1c9b57058', 0, 0);\n")
					sqlfileTitles.write("INSERT INTO text (languageCode, text) VALUES ('en', '" + stoneInfo.name + "');\n")
					sourceFilename = "images/300px/" + stoneInfoImageFilename
					destFilename = "images/300pxUUID/" + nextUuid + ".jpg"
					if Path(destFilename).is_file():
						continue
					copyfile(sourceFilename, destFilename)
					
				else:
					print("----------not found " + stoneInfoImageFilename)
