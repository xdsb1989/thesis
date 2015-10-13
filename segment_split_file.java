import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class segment_split_file {
	
	public static void main(String[] args) throws IOException {
		String orginal_file = "E:/Machine Learning/thesis/new_thesis_data.txt";
		FileReader fr = new FileReader(orginal_file);
		BufferedReader br =  new BufferedReader(fr);
		fr = new FileReader(orginal_file);
		br =  new BufferedReader(fr);
		String Line;
		int number_of_records = 0;
		while ((Line = br.readLine()) != null){
			number_of_records++;
		}
		br.close();
		fr.close();
		
		String record_list [] = new String [number_of_records];
		String class_type[][] = new String [number_of_records][];
		fr = new FileReader(orginal_file);
		br =  new BufferedReader(fr);
		
		number_of_records = 0;
		while ((Line = br.readLine()) != null){
			record_list[number_of_records] = Line;
			class_type[number_of_records] = Line.split(" ");
			number_of_records++;
		}
		br.close();
		fr.close();
		
		
		int break_point=0,end_point=0;
		int test_number = number_of_records * 1/3;
		//for (int i=0;i<class_type.length;i++)
		//	System.out.println(i+" "+class_type[i][class_type[i].length-1]);
		
		while (true){
			Random rd = new Random();
			break_point = rd.nextInt(number_of_records*2/3);
			end_point = break_point + test_number;
			System.out.println(break_point+" "+end_point);
			if (class_type[break_point][class_type[break_point].length-1].equals("No_action") && class_type[end_point][class_type[end_point].length-1].equals("No_action"))
				break;
		}
		
		
		File train_file = new File("E:/Machine Learning/thesis/thesis_data-train.txt");
		FileWriter train_fw = new FileWriter(train_file);
		File test_file = new File("E:/Machine Learning/thesis/thesis_data-test.txt");
		FileWriter test_fw = new FileWriter(test_file);
		
		
		
		int a=0,b=0;
		for (int i=0;i<number_of_records;i++){
			
			if (i >= break_point && i < end_point){
				test_fw.write(record_list[i]+"\n");
				a++;
			}
			else{
				train_fw.write(record_list[i]+"\n");
				b++;
			}
		}
		train_fw.flush();   
		train_fw.close();
		test_fw.flush();   
		test_fw.close();
		//System.out.println(a+"   "+b);
	}
}
