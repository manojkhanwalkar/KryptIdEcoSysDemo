package misc;

import com.amazonaws.services.dynamodbv2.document.Index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Indexer {

    Set<String> wordList = new HashSet<>();
    Map<String,List<IndexedTuple>> index = new HashMap<>();

    public Indexer()
    {
        init();
    }

    private void init()
    {

        List<String> list = List.of("Java", "Security", "Notgoingtobefound", "example", "turotrial");
        wordList.addAll(list);
        list.stream().forEach(s->{

            index.put(s,new ArrayList<>());

        });
    }

    private void buildIndexForFile(File fileName)
    {
        try {

            Map<String,Integer> wordFreq = new HashMap<>();
            wordList.stream().forEach(w->wordFreq.computeIfAbsent(w, (key)->0));

            // add to temp map - word,freq
            // after file is processed then move that to the index with one add per key
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String line = bufferedReader.readLine();
            while (line!=null)
            {
                String[] words = line.split(" ");

                for (String word : words)
                {
                    if (wordList.contains(word))
                    {
                        Integer count = wordFreq.get(word);
                        wordFreq.put(word,count+1);
                    }
                }
                line = bufferedReader.readLine();
            }

            wordFreq.entrySet().stream().forEach((e->{
                if (e.getValue()!=0) {
                    String key = e.getKey();
                    index.get(key).add(new IndexedTuple(e.getValue(), fileName.getName()));
                }
            }));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createIndex(String dirName)
    {
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        for (File file : files )
        {
            System.out.println(file.getName());
            buildIndexForFile(file);
        }

        System.out.println(index);
    }

    public List<String> getFilesMatchingSearch(String... searchWords)
    {
        List<IndexedTuple> result = null;
        for (String word : searchWords)
        {
            List<IndexedTuple> search = index.get(word);
            if (search==null)
                return null;
            result = findIntersection(result,search);
        }

       return  result.stream().map(r->r.getFileName()).collect(Collectors.toList());
    }

    private List<IndexedTuple> findIntersection(List<IndexedTuple> list1, List<IndexedTuple> list2)
    {
        // ignore null lists
        if (list1==null)
            return list2;
        if (list2==null)
            return list1;

        List<IndexedTuple> list = new ArrayList<>();
        list1.stream().forEach(tuple-> {
           int index =  list2.indexOf(tuple);
           if (index>=0)
           {
               list.add(IndexedTuple.combineFrequeny(tuple,list2.get(index)));
           }


        });

        return list;
    }




    public static void main(String[] args) {

        Indexer indexer = new Indexer();
        indexer.createIndex("/home/manoj/data/indexer/");
        System.out.println(indexer.getFilesMatchingSearch("Java", "Notgoingtobefound"));
    }

}
