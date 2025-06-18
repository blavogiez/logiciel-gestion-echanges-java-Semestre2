#!/usr/bin/env python3

# Gery Casiez https://cristal.univ-lille.fr/~casiez
# 2016 - 2022
# version 1.3 (25/05/22)

import sys
import os
import re
import subprocess

def get_dimensions(pathtovideo):
    pattern = re.compile(b'Stream.*Video.* (\d+)x(\d+)')
    try:
        p = subprocess.Popen(['ffmpeg', '-i', pathtovideo],
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        stdout, stderr = p.communicate()
    except:
        print("La commande ffmpeg n'est pas installee sur votre machine")
        print("Lancez la commande sudo 'apt-get install ffmpeg' pour l'installer")
        print("Suivant votre distribution il pourra etre necessaire d'ajouter un repo non officiel")
        print("Plus d'informations disponibles ici: http://ffmpeg.org/download.html")
        exit(-1)
    match = pattern.search(stderr)
    if match:
        x, y = map(int, match.groups()[0:2])
    else:
        x = y = 0
    return {'w': x, 'h': y}


if (len(sys.argv) != 4):
    print("La commande requiert 3 arguments: 1) la video 2) l'image de debut et 3) l'image de fin")
    print("Si possible les images de debut et de fin doivent avoir la meme resolution que la video")
    print("exemple: videomaker video.mov ImageDebut.jpeg ImageFin.jpeg")
    exit(0)
else:
    filenameMovie = sys.argv[1]
    filenameFrontPicture = sys.argv[2]
    filenameBackPicture = sys.argv[3]

    dim = get_dimensions(filenameMovie)
    #print "resolution: " + str(dim['w']) + "x" + str(dim['h'])

    # Check that convert command is available
    try:
        p = subprocess.Popen(['convert', '-v'],
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE)
        stdout, stderr = p.communicate()
    except:
        print("La commande convert n'est pas installee sur votre machine")
        print("Lancez la commande 'sudo apt-get install imagemagick' pour l'installer")
        exit(-1)  

    os.system("rm -f videotmp.mp4 front.mp4 back.mp4 front.jpg back.jpg tmp1.ts tmp2.ts tmp3.ts %sPROCESSED.mp4"%filenameMovie[:-4])

    os.system("ffmpeg -i %s -map 0 -c:v libx264 -c:a copy -pix_fmt yuv420p videotmp.mp4"%filenameMovie)

    os.system("convert %s -gravity center -background white -extent %sx%s front.jpg"%(filenameFrontPicture,dim['w'],dim['h']))
    os.system("convert %s -gravity center -background white -extent %sx%s back.jpg"%(filenameBackPicture,dim['w'],dim['h']))

    os.system("ffmpeg -loop 1 -i front.jpg -c:v libx264 -t 3 -pix_fmt yuv420p front.mp4")
    os.system("ffmpeg -loop 1 -i back.jpg -c:v libx264 -t 3 -pix_fmt yuv420p back.mp4")

    # https://trac.ffmpeg.org/wiki/Concatenate
    os.system("ffmpeg -i front.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts tmp1.ts")
    os.system("ffmpeg -i videotmp.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts tmp2.ts")
    os.system("ffmpeg -i back.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts tmp3.ts")
    os.system("ffmpeg -i \"concat:tmp1.ts|tmp2.ts|tmp3.ts\" -c copy -bsf:a aac_adtstoasc %sPROCESSED.mp4"%filenameMovie[:-4])

    os.system("rm -f videotmp.mp4 front.mp4 back.mp4 front.jpg back.jpg tmp1.ts tmp2.ts tmp3.ts")
