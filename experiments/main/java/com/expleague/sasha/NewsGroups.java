package com.expleague.sasha;

import com.expleague.commons.io.codec.seq.DictExpansion;
import com.expleague.commons.random.FastRandom;
import com.expleague.commons.seq.ByteSeq;
import com.expleague.commons.seq.CharSeq;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

public class NewsGroups {

    private static List<String> fetch20NewsFilenames(final String path) {
        List<String> filenames = new ArrayList<>();
        File mainDir = new File(path);
        for (File dir : mainDir.listFiles()) {
            if (!dir.isDirectory()) {
                continue;
            }
            System.out.println(dir);
            for (File file : dir.listFiles()) {
                filenames.add(dir + "\\" +file.getName());
            }
        }
        return filenames;
    }

    private static List<String> fetch20News(final String path) {
    /*List<String> content = new ArrayList<>();
    File mainDir = new File(path);
    //for (int i = 0; i < 4; i++) {
      //File dir = mainDir.listFiles()[i];
    for (File dir : mainDir.listFiles()) {
      if (!dir.isDirectory()) {
          continue;
      }
      System.out.println(dir);
      for (File file : dir.listFiles()) {
        StringBuilder sb = new StringBuilder();
        try {
          Files.lines(Paths.get(dir + "\\" + file.getName()), Charset.forName("Cp1252")).forEach(x -> {
            sb.append(x);
            sb.append('\n');
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
        content.add(sb.toString());
        //content.add(dir + "\\" + file.getName());
      }
    }*/
        List<String> content = new ArrayList<>();
        List<String> filenames = fetch20NewsFilenames(path);
        for (final String filename : filenames) {
            StringBuilder sb = new StringBuilder();
            try {
                Files.lines(Paths.get(filename), Charset.forName("Cp1252")).forEach(x -> {
                    sb.append(x);
                    sb.append('\n');
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            content.add(sb.toString());
        }
        return content;
    }

    private static void simple20news(final String dir) throws Exception {
        final FastRandom rng = new FastRandom(0);
        //final String DIR = "E:\\YandexDisk\\Саша\\Учеба\\Остальное\\Кураленок\\fastText\\code\\fastText\\data\\my\\my_text";
        //final String DIR = "/home/akhvorov/Yandex.Disk/Саша/Учеба/Остальное/Кураленок/fastText/code/fastText/data/my/ag_news";
        //final String DIR = "E:\\YandexDisk\\Саша\\Учеба\\CSC\\Практика\\data\\20_newsgroups";
        final File toParse = new File(dir + ".test");
        final Set<Character> allCharacters = new HashSet<>();
        long heapSize = Runtime.getRuntime().maxMemory();
        System.out.println("Heap Size = " + heapSize / 1000 / 1000);
        List<String> content = fetch20News(dir);
        for (String text : content) {
            for (int i = 0; i < text.length(); i++) {
                allCharacters.add(text.charAt(i));
            }
        }
        final DictExpansion<Character> expansion = new DictExpansion<>(allCharacters, 20000, System.out);
        int index = 0;
        System.out.println("Content size = " + content.size());
        for (int i = 0; i < 10; i++) {
            for (String text : content) {
                expansion.accept(CharSeq.create(text));
                if (++index % 10000 == 0)
                    try {
                        expansion.printPairs(new FileWriter(new File(dir + "\\pairs.dict")));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                if (index % 1000 == 0)
                    System.out.println("Index: " + index);
            }
            System.out.println(i + "-th iter end");
            System.out.println(expansion.result().size());
            expansion.print(new FileWriter(new File(dir + ".dict")));
        }
    /*for (int i = 0; i < content.size(); i++) {
      CharSeqTools.processLines(new FileReader(content.get(i)), new Consumer<CharSequence>() {
        int index = 0;
        int count = 0;
        @Override
        public void accept(CharSequence arg) {
          if (arg.length() < 150)
            return;
          expansion.accept(CharSeq.create(arg));
          if (++index % 10000 == 0)
            try {
              expansion.printPairs(new FileWriter(new File(DIR + "\\pairs.dict")));
              System.out.println("write to file" + count);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          count++;
        }
      });
    }*/
        System.out.println(expansion.result().size());
        expansion.print(new FileWriter(new File(dir + ".dict")));
        System.out.println();
        System.out.println("END");
    }

    private static ByteSeq textToZipBytes(final List<String> filenames, final String localFile) {
        final List<String> shortNames = filenames.stream()
                .map(x -> x.substring(x.lastIndexOf("\\")))
                .collect(Collectors.toList());
      /*try {
          ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(localFile));
          shortNames.forEach(file -> {
              try {

                  zos.putNextEntry(new ZipEntry(file));
                  zos.closeEntry();
              } catch (IOException e) {
                  System.out.println("file " + file + " not found");
              } catch (IllegalArgumentException ea) {
                  System.out.println(file);
              }
          });
          zos.close();
      } catch (IOException e) {
          System.out.println("file " + localFile + " not open");
      }*/
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(localFile));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(zis, out);
          /*final int bufSize = 2048;
          int count = 0;
          int offset = 0;
          byte data[] = new byte[bufSize];
          while ((count = zis.read(data, offset, bufSize)) != -1) {
              out.write(data);
              offset += bufSize;
          }
          System.out.println("Offset = " + offset + ", len = " + out.toByteArray().length);
          return new ByteSeq(out.toByteArray());*/
            System.out.println(out.toByteArray().length);
            return new ByteSeq(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Exception in reading zip");
        }
    }

    private static void zip20news(final String dir) throws Exception {
        //final String DIR = "E:\\YandexDisk\\Саша\\Учеба\\CSC\\Практика\\data\\20_newsgroups";
        final File toParse = new File(dir + ".test");
        final Set<Byte> allCharacters = new HashSet<>();
        long heapSize = Runtime.getRuntime().maxMemory();
        System.out.println("Heap Size = " + heapSize / 1000 / 1000);
        List<String> filenames = fetch20NewsFilenames(dir);
        ByteSeq byteSeq = textToZipBytes(filenames, dir + "_temp.zip");
        System.out.println("byteSeq is ready");
        System.out.println("seq length = " + byteSeq.length());
        for (int i = 0; i < 256; i++) {
            allCharacters.add((byte)i);
        }
        final DictExpansion<Byte> expansion = new DictExpansion<>(allCharacters, 20000, System.out);
        System.out.println("Content size = " + filenames.size());
        for (int i = 0; i < 2; i++) {
            expansion.accept(byteSeq);
            System.out.println(i + "-th iter end");
            if (expansion.result() != null) {
                System.out.println(expansion.result().size());
                expansion.print(new FileWriter(new File(dir + ".dict")));
            }
        }
        System.out.println();
        System.out.println("END");
    }

    public static void main(String... args) {
        try {
            final String DIR = "E:\\YandexDisk\\Саша\\Учеба\\CSC\\Практика\\data\\20_newsgroups";
            //simple20news(DIR);
            zip20news(DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
