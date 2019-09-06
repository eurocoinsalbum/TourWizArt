import csv
import codecs
import os
from shutil import copyfile
from pathlib import Path

if not (Path("images/300pxUUID")).is_dir():
	os.makedirs("images/300pxUUID")
	
with codecs.open('TourWizArt_extract_datasetObjects.csv', "r", "utf-8") as csvfile:
	with codecs.open('TourWizArt_insert_datasetObjects.sql', "w", "utf-8") as sqlfile:
		datasetObjectsReader = csv.reader(csvfile, delimiter=';', quotechar='"')
		for row in datasetObjectsReader:
			sqlfile.write("INSERT INTO dataset_object tourDatasetUuid, datasetObjectUuid, titleTextId, location, status VALUES ('37d81284-6ee2-4294-8a06-61fdf907a053', '" + row[0] + "', -1, NULL, 'ACTIVE');\n")
			sourceFilename = "images/300px/" + row[1].replace(" ", "_") + ".jpg"
			print(sourceFilename)
			if not Path(sourceFilename).is_file():
				continue

			destFilename = "images/300pxUUID/" + row[0] + ".jpg"
			if Path(destFilename).is_file():
				continue
				
			copyfile(sourceFilename, destFilename)
			
