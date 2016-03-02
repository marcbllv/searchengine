SRC = src/ir/*.java
OUT = bin
PDF_BOX = ./pdfbox

CP = .:$(PDF_BOX):$(OUT)

all:
	javac -d $(OUT) -Xlint:none -cp $(CP) $(SRC)

run:
	java -Xmx1024m -cp $(CP) ir.SearchGUI -d ./davisWiki

noindex:
	java -Xmx1024m -cp $(CP) ir.SearchGUI

clean:
	rm -Rf $(OUT)/*

cleanindex:
	rm -Rf savedindex/*
