import csv
import codecs
import os
from shutil import copyfile
from pathlib import Path

if not (Path("images/300pxUUID")).is_dir():
	os.makedirs("images/300pxUUID")
	
with codecs.open('uuid2id_mapping.csv', "r", "utf-8") as csvfile:
	with codecs.open('uuid2id_mapping.sql', "w", "utf-8") as sqlfile:
		datasetObjectsReader = csv.reader(csvfile, delimiter=';')
		for row in datasetObjectsReader:
			sqlfile.write("INSERT INTO dataset_object (tourDatasetUuid, datasetObjectUuid, titleTextId, location, status, artistUuid, styleTextId, year) VALUES ('37d81284-6ee2-4294-8a06-61fdf907a053', '" + row[0] + "', -1, NULL, 'ACTIVE', '0dd1cdad-8882-4862-9e21-f41ea0c95660', 0, 0);\n")
			sourceFilename = "images/300px/" + row[1] + "_001.jpg"
			print(sourceFilename)
			if not Path(sourceFilename).is_file():
				continue

			destFilename = "images/300pxUUID/" + row[0] + ".jpg"
			if Path(destFilename).is_file():
				continue
				
			copyfile(sourceFilename, destFilename)
			
