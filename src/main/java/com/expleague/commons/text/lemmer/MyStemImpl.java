package com.expleague.commons.text.lemmer;

import com.expleague.commons.seq.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class MyStemImpl implements MyStem {
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.DAYS, new LinkedBlockingQueue<>());
  private final Writer toMyStem;
  private ReaderChopper chopper;

  public MyStemImpl(Path mystemExecutable) {
    try {
      Process mystem = Runtime.getRuntime().exec(mystemExecutable.toString() + " -i --weight -c");
      toMyStem = new OutputStreamWriter(mystem.getOutputStream(), StandardCharsets.UTF_8);
      Reader fromMyStem = new InputStreamReader(mystem.getInputStream(), StandardCharsets.UTF_8);
      chopper  = new ReaderChopper(fromMyStem);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public MyStemImpl(InputStream fromMyStem, OutputStream toMyStem) {
    this.toMyStem = new OutputStreamWriter(toMyStem, StandardCharsets.UTF_8);
    Reader fromMyStemReader = new InputStreamReader(fromMyStem, StandardCharsets.UTF_8);
    chopper = new ReaderChopper(fromMyStemReader);
  }

  @Override
  public List<WordInfo> parse(CharSequence seq) {
    final Task task = new Task(seq);
    final FutureTask<List<WordInfo>> ftask = new FutureTask<>(task, task.answer);
    executor.execute(ftask);
    try {
      final List<WordInfo> result = ftask.get();
      if (task.e instanceof RuntimeException)
        throw (RuntimeException) task.e;
      else if (task.e != null)
        throw new RuntimeException(task.e);
      return result;
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }


  private class Task implements Runnable {
    private final CharSequence request;
    private final List<WordInfo> answer = new ArrayList<>();
    private Exception e;

    private Task(CharSequence request) {
      this.request = request;
    }

    @Override
    public void run() {
      try {
        CharSeqTools.split(request, " ", false);
        toMyStem.append(request);
        toMyStem.append(" ").append("eol").append("\n");
        toMyStem.flush();
        WordInfo next;
        //noinspection EqualsBetweenInconvertibleTypes
        while (!((next = readNext(chopper)).token()).equals("eol")) {
          answer.add(next);
          chopper.eat(' ');
        }
      }
      catch (Exception e) {
        this.e = e;
      }
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    private WordInfo readNext(ReaderChopper chopper) throws IOException {
      final CharSeqBuilder tokenBuilder = new CharSeqBuilder();
      final int delimiter = chopper.chop(tokenBuilder, TOKENS_DELIMITERS);
      final CharSeq token = CharSeq.compact(CharSeqTools.trim(tokenBuilder.build()));
      if (delimiter == ' ')
        return new WordInfo(token);
      final CharSeq info = chopper.chop('}');
      if (CharSeqTools.endsWith(Objects.requireNonNull(info), "??"))
        return new WordInfo(token);
      final List<LemmaInfo> lemmas = new ArrayList<>();
      CharSeqTools.split(info, "|", false).forEach(lemmaInfo -> {
        try {
          ReaderChopper infoChopper = new ReaderChopper(new CharSeqReader(lemmaInfo));
          final CharSeq lemma = CharSeq.compact(infoChopper.chopQuiet(':'));
          if (CharSeqTools.endsWith(lemma, "?"))
            return;

          final double weight = CharSeqTools.parseDouble(infoChopper.chopQuiet('='));
          final CharSeq type = infoChopper.chop(DESCRIPTION_DELIMITERS);
          final PartOfSpeech pos = PartOfSpeech.valueOf(Objects.requireNonNull(type).toString());
          final LemmaInfo.Factory factory = pos.factory.get();
          factory.lemma(lemma, weight);
          CharSeq prop;
          while((prop = infoChopper.chop(DESCRIPTION_DELIMITERS)) != null) {
            factory.accept(prop);
          }
          lemmas.add(factory.build());
        }
        catch (IOException e1) {
          throw new RuntimeException(e1);
        }

      });

      return new WordInfo(token, lemmas);
    }
  }
  private static final BitSet DESCRIPTION_DELIMITERS = ReaderChopper.mask(',', '=');
  public static final BitSet TOKENS_DELIMITERS = ReaderChopper.mask('{', ' ');
}
