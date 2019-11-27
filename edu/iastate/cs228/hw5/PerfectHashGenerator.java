package edu.iastate.cs228.hw5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * A class that generates a perfect hash table.
 *
 * @author
 */
public class PerfectHashGenerator {
	/**
	 * The number of rows in the T1 and T2 tables.
	 * Enough to fit most English words.
	 */
	private static final int TABLE_ROWS = 8;

	/**
	 * The number of columns in the T1 and T2 tables.
	 * Enough to fit all English letters.
	 */
	private static final int TABLE_COLUMNS = 64;

	public static void main(String[] args) throws IllegalArgumentException, IOException {
		if (null == args || 1 > args.length || 3 < args.length)
		{
			System.err.println("Usage: <word list> [prefix] [seed]");
			System.exit(1);
			return;
		}

		Random rng;
		String prefix = "";
		if (args.length > 1) {
			prefix = args[1];
			if (args.length > 2) {
				rng = new Random(Integer.parseInt(args[2]));
			}

			else {
				rng = new Random();
			}

			PerfectHashGenerator gen = new PerfectHashGenerator();
			try {
				gen.generate(args[0], prefix + "CHM92Hash", rng);
			}
			
			catch (IOException e) {
				System.err.println(e);
				System.exit(2);
				return;
			}
		}
		
		else {
			PerfectHashGenerator gen = new PerfectHashGenerator();
			rng = new Random();
			try {
				gen.generate(args[0], prefix + "CHM92Hash", rng);
			}
			
			catch (IOException e) {
				System.err.println(e);
				System.exit(2);
				return;
			}
		}
	}


	/**
	 * Generates the perfect hash table for the words in the indicated file, and
	 * writes the generated code to the appropriate file
	 * ({@code outputClassName + ".java"}).
	 *
	 * @param wordFileName
	 *   the name of the word file
	 * @param outputClassName
	 *   the name of the output class
	 * @param rng
	 *   the random number generator for the generated hash table
	 *
	 * @throws IOException
	 *   if the input file cannot be read or the output file cannot be written
	 * @throws IllegalArgumentException
	 *   if the given output class name is not a valid Java identifier
	 */
	public void generate(String wordFileName, String outputClassName, Random rng) throws IOException, IllegalArgumentException {
		List<String> ALS = readWordFile(wordFileName);
		int[][] table1 = new int[TABLE_ROWS][TABLE_COLUMNS];
		int[][] table2 = new int[TABLE_ROWS][TABLE_COLUMNS];
		Graph g = mapping(table1, table2, (ALS.size()*2) + 1, rng, ALS);
		int[] gArray = g.fillGArray(ALS.size());
		CodeGenerator cg = new CodeGenerator(table1, table2, gArray, (ALS.size()*2) + 1, ALS);
		FileOutputStream fos = new FileOutputStream(new File(outputClassName + ".java"));
		cg.generate(fos, outputClassName);
	}

	/**
	 * Generates the perfect hash table for the given words, and writes the
	 * generated code to the given stream.
	 *
	 * @param words
	 *   the list of words for which to generate a perfect hash table
	 * @param output
	 *   the stream to which to write the generated code
	 * @param outputClassName
	 *   the name of the output class
	 * @param rng
	 *   the random number generator for the generated hash table
	 *
	 * @throws IllegalArgumentException
	 *   if the given output class name is not a valid Java identifier
	 * @throws FileNotFoundException 
	 */
		public void generate(List<String> words, OutputStream output, String outputClassName, Random rng) throws IllegalArgumentException, FileNotFoundException {
			int[][] table1 = new int[TABLE_ROWS][TABLE_COLUMNS];
			int[][] table2 = new int[TABLE_ROWS][TABLE_COLUMNS];
			Graph g = mapping(table1, table2, (words.size()*2) + 1, rng, words);
			int[] gArray = g.fillGArray(words.size());
			CodeGenerator cg = new CodeGenerator(table1, table2, gArray, (words.size()*2) + 1, words);
			cg.generate(output, outputClassName);
		}

	/**
	 * Performs the mapping step for generating the perfect hash table.
	 * Precondition: the list of keys contains no duplicate values.
	 *
	 * @param table1
	 *   the T1 table
	 * @param table2
	 *   the T2 table
	 * @param modulus
	 *   the modulus
	 * @param rng
	 *   the random number generator to use
	 * @param words
	 *   the list of keys for the hash table
	 * @return
	 *   the generated graph
	 *
	 * @throws IllegalArgumentException
	 *   if the modulus is not positive
	 */
	private Graph mapping(int[][] table1, int[][] table2, int modulus, Random rng, List<String> words) throws IllegalArgumentException {
		Graph toRet = null;
		do {
			toRet = new Graph(modulus);
			for (int r = 0; r < TABLE_ROWS; ++r) {
				for (int c = 0; c < TABLE_COLUMNS; ++c) {
					table1[r][c] = rng.nextInt(modulus);
					table2[r][c] = rng.nextInt(modulus);
				}
			}

			for (int i = 0; i < words.size(); ++i) {
				String w = words.get(i);
				int f1 = 0, f2 = 0;
				for (int j = 0; j < w.length(); ++j) {
					f1 += table1[j % TABLE_ROWS][w.charAt(j) % TABLE_COLUMNS];
					f2 += table2[j % TABLE_ROWS][w.charAt(j) % TABLE_COLUMNS];
				}

				f1 %= modulus;
				f2 %= modulus;
				toRet.addEdge(f1, f2, i, w);
			}
		} while (toRet.hasCycle());

		return toRet;
	}

	/**
	 * Reads the indicated file, making a list containing the lines within it.
	 *
	 * @param fileName
	 *   the file to read
	 * @return
	 *   a list containing the lines of the indicated file
	 *
	 * @throws FileNotFoundException
	 *   if the indicated file cannot be read
	 */
	private List<String> readWordFile(String fileName) throws FileNotFoundException {
		ArrayList<String> ALS = new ArrayList<String>();
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNextLine()) {
			ALS.add(scan.nextLine());
		}

		scan.close();
		return ALS;
	}
}