import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class extra_classes {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		
		String name = "thesis_data-test.txt";
		String orginal_file = "E:/Machine Learning/thesis/";
		FileReader fr = new FileReader(orginal_file+name);
		BufferedReader br =  new BufferedReader(fr);
		
		String Line;
		int number_of_records = 0;
		while ((Line = br.readLine()) != null){
			number_of_records++;
		}
		String record_list [] = new String [number_of_records];
		br.close();
		fr.close();
		fr = new FileReader(orginal_file+name);
		br =  new BufferedReader(fr);
		number_of_records = 0;
		while ((Line = br.readLine()) != null){
			record_list[number_of_records] = Line;
			number_of_records++;
		}
		br.close();
		fr.close();
		
		File extract_file = new File(orginal_file+"extract-"+name);
		FileWriter fw = new FileWriter(extract_file);
		int action=0;
		for (int i=0;i<number_of_records;i++){
			String str [] = record_list[i].split(" ");
			if (!str[str.length-1].equals("No_action")){
				action++;
				fw.write(record_list[i]+"\n");
			}
		}
		
		Random rd = new Random();
		int no_action = 0;
		for (int i=0;i<number_of_records;i++){
			String str [] = record_list[i].split(" ");
			if ( str[str.length-1].equals("No_action") ){
				int a = rd.nextInt(50);
				if (a == 1){
					fw.write(record_list[i]+"\n");
					no_action++;
					if (no_action == action)
						break;
				}
			}
		}
		fw.flush();
		fw.close();
		System.out.println(action+"    "+no_action);
	}
}
