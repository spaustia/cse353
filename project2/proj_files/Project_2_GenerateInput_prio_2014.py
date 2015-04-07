#!/usr/bin/python

#
# CSE/IT 353: Data and Computer Communications
# Project 2: Token Ring Emulation
# GenerateInput.py: Input file generator
#
# Written by: Juston Moore
# September 28, 2009
#
# -------------------------------------------------------------
# Modified by: Rachel Tucker
# September 23, 2013
# September 28, 2014
#
# This program generates frames with node priority.  All frames
# for each node should have the same priority.  This has been
# changed from frame priority in 2013.
#
# Each line in this file corresponds to the input frame format:
#   <Destination_Address>,<Node Priority>,<Data_Size>,<data>

import sys
import random

# Check for command-line argument
if len(sys.argv) != 2:
	nodes = 5
#	print "Usage: GenerateInput.py [Number of Nodes]"
#	sys.exit()

if len(sys.argv) == 2:
	nodes = int(sys.argv[1])

# Make sure that the specified number of nodes is correct
if nodes < 2 or nodes > 254:
	print "You must have at least 2 nodes and no more than 254 nodes in your token ring."
	sys.exit()

# Create entries for each node
for node in range(1, nodes+1):
	file = open("input-file-" + str(node), 'w')

	# Generate a random number of entries for each node, from 1 to 1000
	entries = random.randint(1, 300)

	# Generate node priority between 0 and 7 for each node
	priority = random.randint(0, 7)

	for entry in range(0, entries):
		# Randomly generate the destination node NOT the same as the source
		dest = node
		while dest == node:
			dest = random.randint(1, nodes)

		# Randomly generate a PDU size up to 254
		size = random.randint(1, 254)

		# Write file entry
		file.write(str(dest) + ',' + str(priority) + ',' + str(size) + ',')
		for data in range(0, size):
			# Write random PDU data (Uppercase ASCII)
			char = chr(random.randint(65,90))
			file.write(char)
		file.write("\n")