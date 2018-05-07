package com.expleague.sasha;

import com.expleague.commons.io.codec.seq.DictExpansion;
import com.expleague.commons.io.codec.seq.Dictionary;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NewsGroups {

    private static List<String> fetchFilenames(final String path) {
        List<String> filenames = new ArrayList<>();
        File mainDir = new File(path);
        for (File dir : mainDir.listFiles()) {
            if (!dir.isDirectory()) {
                continue;
            }
            //System.out.println(dir);
            for (File file : dir.listFiles()) {
                filenames.add(dir + "/" + file.getName());
            }
        }
        System.out.println("read all filenames in " + mainDir.getPath());
        return filenames;
    }

    private static String fetchFile(final String filename) {
        try {
            StringBuilder sb = new StringBuilder();
            try (Stream<String> lines = Files.lines(Paths.get(filename), Charset.forName("Latin1"))) {
                lines.forEach(x -> {
                    //Files.lines(Paths.get(filename), Charset.forName("UTF8")).forEach(x -> {
                    sb.append(x);
                    sb.append('\n');
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println(filename);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static List<String> fetchFiles(final List<String> filenames) {
        List<String> content = new ArrayList<>();
        for (final String filename : filenames) {
            content.add(fetchFile(filename));
        }
        return content;
    }

    private static void simple20news(final String dir, final int dictSize, final int iterNum) throws IOException {
        final FastRandom random = new FastRandom(0);
        final Set<Character> allCharacters = new HashSet<>();
        long heapSize = Runtime.getRuntime().maxMemory();
        System.out.println("Heap Size = " + heapSize / 1000 / 1000);
        String trainName = "/train";
        String testName = "/test";
        List<String> filenames = fetchFilenames(dir + trainName);
        List<String> testFilenames = fetchFilenames(dir + testName);
        System.out.println("train: " + filenames.size() + ", test: " + testFilenames.size());
        final List<CharSeq> content = filenames.stream().map(name -> {
            try {
                return new FileReader(name);
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).flatMap(CharSeqTools::lines).map(CharSeq::compact).collect(Collectors.toList());
        final List<CharSeq> testContent = testFilenames.stream().map(NewsGroups::fetchFile).map(CharSeq::compact).collect(Collectors.toList());
        content.forEach(text -> text.forEach(allCharacters::add));
        testContent.forEach(text -> text.forEach(allCharacters::add));
        /*System.out.println("alphabet:");
        allCharacters.forEach(x -> System.out.print(x + " "));
        System.out.println();*/
        final DictExpansion<Character> expansion = new DictExpansion<>(allCharacters, dictSize, System.out);
        for (int i = 0; i < iterNum; i++) {
            IntStream.range(0, content.size()).parallel().forEach(idx -> expansion.accept(content.get(random.nextInt(content.size()))));
            System.out.println(i + "-th iter end");
        }

        System.out.println();
        System.out.println("dict is constructed");
        writeBoW(content.stream().map(x -> (Seq<Character>)x).collect(Collectors.toList()),
                expansion.result(), filenames, dir + trainName, "", true);
        System.out.println("Train BoW have written");
        content.clear();
        writeBoW(testContent.stream().map(x -> (Seq<Character>)x).collect(Collectors.toList()),
                expansion.result(), testFilenames, dir + testName, "", false);
        System.out.println("Test BoW have written");
        System.out.println("END");
    }

    private static void simpleOhsumed(final String dir, final int dictSize, final int iterNum) throws IOException {
        final FastRandom random = new FastRandom(0);
        final Set<Character> allCharacters = new HashSet<>();
        long heapSize = Runtime.getRuntime().maxMemory();
        System.out.println("Heap Size = " + heapSize / 1000 / 1000);
        String trainName = "/train";
        List<String> filenames = fetchFilenames(dir + trainName);
        System.out.println("train: " + filenames.size());
        final List<CharSeq> content = filenames.stream().map(NewsGroups::fetchFile).map(CharSeq::create).collect(Collectors.toList());
        for (CharSeq text : content) {
            for (int i = 0; i < text.length(); i++) {
                allCharacters.add(text.charAt(i));
            }
        }
        final DictExpansion<Character> expansion = new DictExpansion<>(allCharacters, dictSize, System.out);
        for (int i = 0; i < iterNum; i++) {
            for (int j = 0; j < content.size(); j++) {
                expansion.accept(content.get(random.nextInt(content.size())));
            }
            System.out.println(i + "-th iter end");
        }

        System.out.println();
        System.out.println("dict is constructed");
        writeBoW(content.stream().map(x -> (Seq<Character>)x).collect(Collectors.toList()),
                expansion.result(), filenames, dir + trainName, "", true);
        System.out.println("Train BoW have written");
        content.clear();
        System.out.println("END");
    }

    private static byte[] changeByteSize(byte[] bytes, int byteSize) {
        if (byteSize == 8) {
            return bytes;
        }
        StringBuilder sb = new StringBuilder();
        int offset = byteSize - (bytes.length * 8) % byteSize;
        for (int i = 0; i < offset; i++) {
            sb.append('0');
        }
        for (byte b : bytes) {
            for (int j = 0; j < 8; j++) {
                if ((b & (1 << 7)) == 0) {
                    sb.append('0');
                } else {
                    sb.append('1');
                }
                b <<= 1;
            }
        }
        String byteStr = sb.toString();
        byte[] newBytes = new byte[byteStr.length() / byteSize];
        int k = 0;
        for (int i = 0; i < newBytes.length; i++) {
            byte b = 0;
            int j = 0;
            while (j < byteSize) {
                b <<= 1;
                if (byteStr.charAt(k) == '1') {
                    b |= 1;
                }
                j++;
                k++;
            }

            newBytes[i] = b;
        }
        return newBytes;
    }

    private static ByteSeq readZipFile(final String filename, final String tempZipFile, int byteSize) throws IOException {
        final String file = filename.substring(filename.lastIndexOf("/"));
        String tempFileName = filename.substring(0, filename.lastIndexOf("/"));
        tempFileName = tempFileName.substring(0, tempFileName.lastIndexOf("/"));
        tempFileName = tempFileName.substring(0, tempFileName.lastIndexOf("/"));
        tempFileName += "/tempFile";
        Files.copy(new File(filename).toPath(), new File(tempFileName).toPath(),
                new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile));
        zos.putNextEntry(new ZipEntry(file));
        zos.closeEntry();

        FileInputStream fis = new FileInputStream(tempZipFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final int bufSize = 256;
        int count;
        byte data[] = new byte[bufSize];
        while ((count = fis.read(data)) != -1) {
            out.write(data, 0, count);
        }
        return new ByteSeq(changeByteSize(out.toByteArray(), byteSize));
    }

    private static ByteSeq readByteFile(final String filename, int byteSize) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            final int bufSize = 256;
            int count;
            byte data[] = new byte[bufSize];
            while ((count = fis.read(data)) != -1) {
                out.write(data, 0, count);
            }
            return new ByteSeq(changeByteSize(out.toByteArray(), byteSize));
        } catch (IOException e) {
            System.out.println("file " + filename + " is not ungzip");
            e.printStackTrace();
            return null;
        }
    }

    private static <T extends Comparable<T>> void writeBoW(final List<Seq<T>> seqs,
                                                           final Dictionary<T> dict,
                                                           final List<String> filenames,
                                                           final String dir,
                                                           final String zipType,
                                                           final boolean isTrain) {
        int errorsNum = 0;
        int zipTypeLen = 0;
        if (zipType != null) {
            zipTypeLen = zipType.length();
        }
        for (int i = 0; i < seqs.size(); i++) {
            final Map<Integer, Integer> bow = new HashMap<>();
            /*for (int gram = 0; gram < dict.size(); gram++) {
                bow.put(gram, 0);
            }*/
            final IntSeq intSeq = dict.parse(seqs.get(i));
            for (final Integer key : intSeq.stream().sorted().boxed().collect(Collectors.toList())) {
                bow.put(key, bow.getOrDefault(key, 0) + 1);
            }
            final String first = dir.substring(0, dir.lastIndexOf('/'));
            //final String second = filenames.get(i).substring(dir.length(), filenames.get(i).indexOf('/', dir.length() + 1));
            //final String newDir = first + "/bows" + second;
            //System.out.println(filenames.get(i) + ", " + dir);
            String newDir = first + "/bows"; // + second;
            String filename = first + "/bows";
            if (isTrain) {
                newDir += "-train";
                filename += "-train";
            } else {
                newDir += "-test";
                filename += "-test";
            }
            filename += filenames.get(i).substring(dir.length(), filenames.get(i).length() - zipTypeLen) + ".bow";
            new File(newDir).mkdir();
            File subDir = new File(filename.substring(0, filename.lastIndexOf('/')));
            if (!subDir.exists()) {
                subDir.mkdir();
            }
            boolean hasWritten = false;
            boolean hasError = false;
            while (!hasWritten) {
                try (PrintStream printStream = new PrintStream(new FileOutputStream(filename))) {
                    for (final Map.Entry<Integer, Integer> entry : bow.entrySet()) {
                        printStream.println(entry.getKey() + "\t" + entry.getValue());
                    }
                    hasWritten = true;
                } catch (IOException e) {
                    hasError = true;
                    boolean isDelete = new File(filename).delete();
                    if (isDelete) {
                        System.out.println(filename + " is deleted");
                    } else {
                        System.out.println(filename + " not deleted");
                    }
                    //e.printStackTrace();
                }
                if (hasError) {
                    errorsNum++;
                }
            }
        }
        System.out.println("Errors = " + errorsNum);
    }

    private static void byte20news(final String dir, final int byteSize, final int dictSize, final int iterNum, final String zipType) throws IOException {
        final FastRandom random = new FastRandom(0);
        final Set<Byte> alphabet = new HashSet<>();
        long heapSize = Runtime.getRuntime().maxMemory();
        System.out.println("Heap Size = " + heapSize / 1000 / 1000);
        String trainName = "train";
        String testName = "test";
        if (zipType != null) {
            trainName += zipType;
            testName += zipType;
        }
        List<String> filenames = fetchFilenames(dir + trainName);
        //filenames = filenames.subList(0, 100); //!!!
        List<String> testFilenames = fetchFilenames(dir + testName);
        //testFilenames = testFilenames.subList(0, 100); //!!!
        List<ByteSeq> byteSeqs = filenames.stream()
                .map(filename -> readByteFile(filename, byteSize))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Seq<Byte>> testByteSeqs = testFilenames.stream()
                .map(x -> (Seq<Byte>)(readByteFile(x, byteSize)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (int i = 0; i < Math.pow(2, byteSize); i++) {
            alphabet.add((byte)i);
        }
        List<Integer> dictSizesSeq = new ArrayList<>();
        System.out.println("start dict expansion");
        final DictExpansion<Byte> expansion = new DictExpansion<>(alphabet, dictSize, System.out);
        for (int i = 0; i < iterNum; i++) {
            for (int j = 0; j < filenames.size(); j++) {
                expansion.accept(byteSeqs.get(random.nextInt(filenames.size())));
            }
            System.out.println(i + "-th iter end");
            if (expansion.result() != null) {
                dictSizesSeq.add(expansion.result().size());
                expansion.print(new FileWriter(new File(dir + "train_" + byteSize + ".dict")));
            }
            /*if (expansion.result().size() >= dictSize) {
                break;
            }*/
            /*if (i > 1 && i % 50 == 0) {
                writeBoW(byteSeqs.stream().map(x -> (Seq<Byte>)x).collect(Collectors.toList()),
                        expansion.result(), filenames, dir + "train-gzip", zipType, true);
                System.out.println("Train BoW have written");
                writeBoW(testByteSeqs, expansion.result(), testFilenames, dir + "test-gzip", zipType, false);
                System.out.println("Test BoW have written");
            }*/
        }
        System.out.println();
        System.out.println("dict is constructed");
        System.out.println("dict sizes: " + dictSizesSeq);
        writeBoW(byteSeqs.stream().map(x -> (Seq<Byte>)x).collect(Collectors.toList()),
                expansion.result(), filenames, dir + trainName, zipType, true);
        System.out.println("Train BoW have written");

        writeBoW(testByteSeqs, expansion.result(), testFilenames, dir + testName, zipType, false);
        System.out.println("Test BoW have written");
        System.out.println("END");
    }

    public static void main(String... args) {
        final String dir = "/Users/solar/data/text_classification/20news-bydate-normalized";
        final String[] collections = new String[]{"20newsgroups", "aclImdb", "ohsumed-all", "reuters"};
        //int byteSize = 8;
        int dictSize = 8000;
        int iterNum = 50;
        String zipType = "";
        try {
            simple20news(dir /*+ collections[0] + "-norm"*/, dictSize, iterNum);
            //simpleOhsumed(dir + collections[2], dictSize, iterNum); //iterNum = 10
            //byte20news(dir, byteSize, dictSize, iterNum, zipType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}