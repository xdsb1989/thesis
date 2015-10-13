import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Random_forest {
	static class Attribute_list{//this is the class to store each attribute.If the "continuous" is 1,then jump to Continuous_Attribute_list
		String attribute_range [];
		int continuous;
	}

	static class Continuous_Attribute_list{//store all the continuous attribute and the attribute position
		String name;
		int position;
		double thresholds;
	}

	static class Value_And_Class{//use this class to sort the continue attribute
		double value;
		String class_type;
	}

	static class rule{//for each  class,it is an expression which contains the attribute_name,attribute index, continuous flag,....and so on
		String attribute_name;
		String class_name;
		int attri_index;
		int final_class;
		double rule_accuracy;
		int continuous;
		String value;
		int fatherindex;
		double thresholds;
		int flag_small;
	}

	static class Tree_node{//for each node,it has an continuous flag indicate,name of attribute,choosing value or thresholds
		String attribute;
		int index;
		String tpye;
		String values [];
		int range;
		Tree_node value_range[];
		Tree_node large;
		Tree_node small;
		int continuous;
		double thresholds;
		int level;
		double Prior_MDL;
		String default_type;
		int class_compare[];
	}
	
	static int number_slap = 0;//number of actual slapping 
	static int number_drink = 0;//number of actual drinking
	static int number_none = 0;//number of no action
	
	static int max_level = 0;
	static int not_match = 0;
	public static void main(String args[]) throws IOException{
		String attri_filename;
		String train_filename;
		String test_filename;
		Scanner in = new Scanner(System.in);
		/*
		System.out.println("Input the attributes file name:");
		attri_filename = in.nextLine();
		System.out.println("Input the training file name:");
		train_filename = in.nextLine();
		System.out.println("Input the testing file name:");
		test_filename = in.nextLine();
		*/
		attri_filename = "E:/Machine Learning/thesis/thesis_data-attr.txt";
		train_filename = "E:/Machine Learning/thesis/thesis_data-train.txt"; //extract-
		test_filename = "E:/Machine Learning/thesis/thesis_data-test.txt";  //extract-
		
		FileReader fr = new FileReader(attri_filename);
		BufferedReader br =  new BufferedReader(fr);
		ArrayList<Attribute_list> attribute_list = new ArrayList<Attribute_list>();
		
		String Line;
		while ((Line = br.readLine()) != null){
			Attribute_list node = new Attribute_list();
			node.attribute_range = Line.split(" ");
			attribute_list.add(node);//the attribute_list contains all the attributes and the value range of each attribute
		}
		br.close();
		fr.close();
		
		String Class_type[] = new String [attribute_list.get(attribute_list.size()-1).attribute_range.length-1];//store the class type
		for (int i=0, j=1;i<attribute_list.get(attribute_list.size()-1).attribute_range.length-1;i++,j++)
			Class_type[i] = attribute_list.get(attribute_list.size()-1).attribute_range[j];//using the last line to assign the classes to the array
		
		for (int i=0; i < attribute_list.size()-1; i++){
			if (attribute_list.get(i).attribute_range[1].equals("continuous"))
				attribute_list.get(i).continuous = 1;
			else
				attribute_list.get(i).continuous = 0;
		}
		
		fr = new FileReader(train_filename);
		br =  new BufferedReader(fr);
		int examples = 0;
		while ((Line = br.readLine()) != null){
			examples++;
		}
		String original_train [][] = new String [examples][];
		br.close();
		fr.close();
		fr = new FileReader(train_filename);
		br =  new BufferedReader(fr);
		examples = 0;
		while ((Line = br.readLine()) != null){
			original_train[examples] = Line.split(" ");
			examples++;
		}
		br.close();
		fr.close();
		fr = new FileReader(test_filename);
		br =  new BufferedReader(fr);
		int test_number = 0;
		while ((Line = br.readLine()) != null){
			test_number++;
		}
		String test [][] = new String [test_number][];
		br.close();
		fr.close();
		fr = new FileReader(test_filename);
		br =  new BufferedReader(fr);
		test_number = 0;
		while ((Line = br.readLine()) != null){
			test[test_number] = Line.split(" ");
			test_number++;
		}
		br.close();
		fr.close();
		
		int number_of_actions = 0;//how many slapping and drinking together
		for (int i=0;i<examples;i++)
			if (!original_train[i][original_train[i].length-1].equals("No_action"))
				number_of_actions++;
		
		int number_of_trees = 100;//how many trees to build
		//number_of_trees = examples/number_of_actions - 1;
		double noaction_choose_rate = (double) number_of_actions/(examples - number_of_actions);
		System.out.println(noaction_choose_rate);
		int inside_index = 0;
		String action_set [][] = new String [number_of_actions][];
		for (int i=0;i<examples;i++){
			if (!original_train[i][original_train[i].length-1].equals("No_action")){
				action_set [inside_index] = new String [original_train[i].length];
				for (int j = 0;j<original_train[i].length;j++)
					action_set[inside_index][j] = original_train[i][j];
				inside_index++;
				if (inside_index == number_of_actions)
					break;
			}
		}
		
		String divid_noaction_set[][][] = new String [number_of_trees][number_of_actions][];
			
		int index_tree = 0;//which part it is right now
		inside_index = 0;//which row it is right now in particular part
		
		while (index_tree < number_of_trees){
			for (int i=0;i<examples;i++){
				if (original_train[i][original_train[i].length-1].equals("No_action")){
					Random rd = new Random();
					double pass_rate = rd.nextDouble();
					if (pass_rate < noaction_choose_rate){
						divid_noaction_set[index_tree][inside_index] = new String [original_train[i].length]; 
						for (int j = 0;j<original_train[i].length;j++)
							divid_noaction_set[index_tree][inside_index][j] = original_train[i][j];
						inside_index++;
						if (inside_index == number_of_actions){
							index_tree++;
							inside_index = 0;
							break;
						}
					}
				}
			}
		}
		
		int training_number = number_of_actions + number_of_actions;
		Tree_node root[] = new Tree_node[number_of_trees];
		for (int ith_tree = 0;ith_tree<number_of_trees;ith_tree++){//from this part, begins to build multiple trees
			
			int length = original_train[0].length;
			String train [][] = new String [training_number][];
			
			int row_1 = 0;
			int row_2 = 0;
			for (int i=0;i<training_number;i++){
				train[i] = new String [length];
				if (i<number_of_actions){
					for (int j=0;j<length;j++)
						train[i][j] = divid_noaction_set[ith_tree][row_1][j];
					row_1++;
				}
				else{
					for (int j=0;j<length;j++)
						train[i][j] = action_set[row_2][j];
					row_2++;
				}
			}
			
			
			ArrayList<Continuous_Attribute_list> continuous_attribute_list = new ArrayList<Continuous_Attribute_list>();
			for (int i=0;i<attribute_list.size()-1;i++){
				if (attribute_list.get(i).continuous == 1){//for continuous attribute,then create a number of thresholds in continuous_attribute_list
					String attribute_thresholds_name = attribute_list.get(i).attribute_range[0];//save the name of the attribute
					Value_And_Class array[] = new Value_And_Class[training_number];
					for (int j=0;j<training_number;j++)
						array[j] = new Value_And_Class();
					for (int j=0;j<training_number;j++){
						array[j].value = Double.parseDouble(train[j][i]);
						array[j].class_type = train[j][train[j].length-1];
					}
					double temp_value;
					String temp_class;
					for (int j=0;j<training_number-1;j++){
						for (int k=0;k<training_number-1;k++){
							if (array[k].value>array[k+1].value){
								temp_value = array[k].value;
								temp_class = array[k].class_type;
								array[k].value = array[k+1].value;
								array[k].class_type = array[k+1].class_type;
								array[k+1].value = temp_value;
								array[k+1].class_type = temp_class;
							}
						}
					}
					
					DecimalFormat df = new DecimalFormat("#.000"); 
					for (int j=1;j<training_number;j++){
						if (!array[j].class_type.equals(array[j-1].class_type) && array[j].value != array[j-1].value){
							double thresholds = Double.parseDouble(df.format((array[j].value + array[j-1].value)/2));
							Continuous_Attribute_list node = new Continuous_Attribute_list();
							node.name = attribute_thresholds_name;
							node.position = i;
							node.thresholds = thresholds;
							continuous_attribute_list.add(node);
						}
					}
				}
			}
			
			int number_of_concreat_attribute = 0;
			for (int i=0;i<attribute_list.size()-1;i++)//count how many available no continuous attributes
				if (attribute_list.get(i).continuous == 0)
					number_of_concreat_attribute++;
			
			System.out.println("attributes: "+number_of_concreat_attribute+" continuous attributes are: "+continuous_attribute_list.size());
			double rate_of_attribute_choose = 1.0;
			int number_of_random_attribute = (int) (rate_of_attribute_choose * (number_of_concreat_attribute + continuous_attribute_list.size()));
			int get_random_attribute = 0;
			
			System.out.println("number_of_random_attribute: "+number_of_random_attribute);
			
			int attribute_index[] = new int [attribute_list.size()-1];
			int attribute_number = 0;
			for (int i=0;i<attribute_list.size()-1;i++)
				if (attribute_list.get(i).continuous == 0){
					Random rd = new Random();
					if (rd.nextDouble() < rate_of_attribute_choose){
						attribute_index[i] = 1;
						attribute_number++;
						get_random_attribute++;
						if (get_random_attribute == number_of_random_attribute)
							break;
					}
				}
			
			int temp_continuous_attribute_index [] = new int [continuous_attribute_list.size()];
			int continuous_attribute_number = number_of_random_attribute - get_random_attribute;
			int continuous_attribute_index [] = new int [continuous_attribute_number];
			
			while (true){
				int flag = 1;
				for (int i=0;i<continuous_attribute_list.size();i++){
					Random rd = new Random();
					if (rd.nextDouble() < rate_of_attribute_choose && temp_continuous_attribute_index[i] != 1){
						temp_continuous_attribute_index[i] = 1;
						get_random_attribute++;
						if (get_random_attribute == number_of_random_attribute){
							flag = 0;
							break;
						}
					}
				}
				if (flag == 0)
					break;
			}
			ArrayList<Continuous_Attribute_list> new_continuous_attribute_list = new ArrayList<Continuous_Attribute_list>();
			for (int i=0;i<continuous_attribute_list.size();i++){
				if (temp_continuous_attribute_index[i] == 1){
					Continuous_Attribute_list node = new Continuous_Attribute_list();
					node.name = continuous_attribute_list.get(i).name;
					node.position = continuous_attribute_list.get(i).position;
					node.thresholds = continuous_attribute_list.get(i).thresholds;
					new_continuous_attribute_list.add(node);
				}
			}
			for (int i=0;i<new_continuous_attribute_list.size();i++)
				continuous_attribute_index[i] = 1;
			
			//System.out.println("number of all attributes: "+ continuous_attribute_list.size());
			//System.out.println("number of attributes: "+ continuous_attribute_number);
			//for (int i=0;i<new_continuous_attribute_list.size();i++)
			//	System.out.println(new_continuous_attribute_list.get(i).name+" "+new_continuous_attribute_list.get(i).thresholds);
				
			root[ith_tree] = new Tree_node();
			Build_tree(root[ith_tree],attribute_list,train,training_number,
					attribute_list.size()-1, continuous_attribute_number,attribute_index,attribute_number + continuous_attribute_number,Class_type,
					continuous_attribute_index,new_continuous_attribute_list,"",1);
			System.out.println("tree is:"+ith_tree);
			String str1 = "|-";
			String str2 = "";
			Print_tree(root[ith_tree],str1,str2);
		}
		
		//String str1 = "|-";
		//String str2 = "";
		//Print_tree(root[0],str1,str2);
		
		String result[] = new String[test_number];//store the predicted results in a String array
		int confidence[][] = new int[test_number][3];
		//System.out.printf("Slap\tDrink\tNo_action\tActual value\n");
		Trees_prediction(root,test,result,Class_type,attribute_list,confidence);
		/*
		System.out.println("Actural   \tPredict");
		for (int i = 0;i<test_number;i++){
			System.out.print(test[i][test[i].length-1]+"   \t"+result[i]);
			if (!result[i].equals(test[i][test[i].length-1]))
				System.out.print("\tnot match");
			System.out.println();
		}*/
		String before_processing [] = new String [test_number];
		for (int i=0;i<before_processing.length;i++)
			before_processing[i] = result[i];
		
		//human_logical_sequence(result,test,before_processing);
		//auto_analyze_sequence(result,original_train,before_processing,test,confidence);
		for (int j=0;j<result.length;j++){
			System.out.printf("%s   \t%s    \t%s   \t%d:%d:%d  ",
					before_processing[j],result[j],test[j][test[j].length-1],confidence[j][0], confidence[j][1], confidence[j][2]);
			if (!before_processing[j].equals(test[j][test[j].length-1]))
				System.out.printf("\t not_match\n");
			else
				System.out.println();
		}
		
		
		int predict_slap = 0;
		int predict_drink = 0;
		int predict_slap_or_drink = 0;
		for (int i = 0;i<test_number;i++){
			if (test[i][test[i].length-1].equals("Slap_head")){
				number_slap++;
				if (result[i].equals("Slap_head"))
					predict_slap++;
			}
			else if (test[i][test[i].length-1].equals("Drink")){
				number_drink++;
				if (result[i].equals("Drink"))
					predict_drink++;
			}
			else if (test[i][test[i].length-1].equals("No_action")){
				number_none++;
				if (!result[i].equals("No_action"))
					predict_slap_or_drink++;
			}
		}
		double true_positive_slap = (double)predict_slap/number_slap;
		double true_positive_drink = (double)predict_drink/number_drink;
		double false_positive_noaction = (double)predict_slap_or_drink/number_none;
		System.out.printf("true positive rate for slaping is: %.3f\n",true_positive_slap);
		System.out.printf("true positive rate for drinking is: %.3f\n",true_positive_drink);
		System.out.printf("false positive rate for slaping and drinking is: %.3f\n",false_positive_noaction);
		
		
		//int cost_score = cost_calculate(result,test);
		//System.out.println("the cost socre is:"+cost_score);
		int correct=0;
		for (int i = 0;i<test_number;i++)
			if (result[i].equals(test[i][test[i].length-1]))
				correct++;
		double test_accuracy =(double) correct/test_number;
		/*
		String train_result[] = new String[examples];//store the predicted results in a String array
		for (int i = 0;i<examples;i++)
			train_result[i] = Predict(root, train[i], Class_type);
		correct=0;
		for (int i = 0;i<examples;i++)
			if (train_result[i].equals(train[i][train[i].length-1]))
				correct++;
		double train_accuracy =(double) correct/examples;
		System.out.println("the accuracy on train set is:"+train_accuracy);*/
		System.out.println("the accuracy on test set is:"+test_accuracy);
	}
	

	private static void auto_analyze_sequence(String[] result, String[][] original_train, 
				String[] before_processing, String [][]test,int [][]confidence) {
		int drink_min = 10;
		int drink_max = 0;
		int slap_min = 10;
		int slap_max = 0;
		int gap_min = 0;
		
		int i=0;
		int flag = 0;
		int number_gap = 0;
		int time = 0;
		while (i<original_train.length){
			//System.out.println(original_train[i][original_train[i].length-1]);
			
			if (original_train[i][original_train[i].length-1].equals("Drink")){
				//System.out.println("D");
				//System.out.println(number_gap);
				flag = 1;
				time++;
				int count = 0;
				int j=i;
				
				if (time == 2)
					gap_min = number_gap;
				else if (time > 2)
					if (gap_min > number_gap)
						gap_min = number_gap;
				
				
				while (original_train[j][original_train[j].length-1].equals("Drink")){
					count++;
					j++;
				}
				if (drink_min > count)
					drink_min = count;
				if (drink_max < count)
					drink_max = count;
				i = j;
				number_gap = 0;
				continue;
			}
			
			else if (original_train[i][original_train[i].length-1].equals("Slap_head")){
				//System.out.println("SSSS");
				//System.out.println(number_gap);
				flag = 1;
				time++;
				int count = 0;
				int j=i;
				
				if (time == 2)
					gap_min = number_gap;
				else if (time > 2)
					if (gap_min > number_gap)
						gap_min = number_gap;
				
				while (original_train[j][original_train[j].length-1].equals("Slap_head")){
					count++;
					j++;
				}
				if (slap_min > count)
					slap_min = count;
				if (slap_max < count)
					slap_max = count;
				i = j;
				number_gap = 0;
				continue;
			}
			if (flag == 1)
				number_gap++;
			i++;
			
		}
		if (drink_min == 1)
			drink_min = 2;
		System.out.printf("%d %d %d %d %d\n",gap_min,drink_min,drink_max,slap_min,slap_max);
		i = 0;
		time = 0;
		number_gap = 0;
		System.out.println(result.length);
		while (i<result.length){
			
			if (result[i].equals("Slap_head")){
				if (time == 0){
					int j = i;
					int count = 0;
					while (result[j].equals("Slap_head")){
						count++;
						j++;
					}
					if (count >= slap_min && count <= slap_max){//if the range belongs to this area
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					else if (count > slap_max){
						//System.out.println("second");
						i=i+slap_max;
						number_gap = 0;
						/*int stop = i + count;
						//i = i + slap_max;
						//number_gap = 0;
						//while (i < stop){
							result[i] = "No_action";
							i++;
							number_gap++;
						}
						time++;*/
						continue;
					}
					else{//if the range not belongs to this area
						while (i<j){
							result[i] = "No_action";
							i++;
						}
						number_gap = number_gap + count;
					}
				}
				else if (time > 0 && number_gap < gap_min){
					result[i] = "No_action";
					number_gap++;
					i++;
				}
				else if (time > 0 && number_gap >= gap_min){
					int j = i;
					int count = 0;
					while (j<result.length && result[j].equals("Slap_head")){
						count++;
						j++;
					}
					if (count >= slap_min  && count <= slap_max){//if the range belongs to this area
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					else if (count > slap_max){
						//System.out.println("second");
						i=i+slap_max;
						number_gap = 0;
						/*
						int stop = i + count;
						i = i + slap_max;
						number_gap = 0;
						while (i < stop){
							result[i] = "No_action";
							i++;
							number_gap++;
						}
						time++;*/
						continue;
					}
					else{//if the range not belongs to this area
						while (i<j){
							result[i] = "No_action";
							i++;
						}
						number_gap = number_gap + count;
					}
				}
			}
			////////////////////////////////////////////////////////////////////
			else if (result[i].equals("Drink")){
				if (time == 0){
					int j = i;
					int count = 0;
					int continue_flag = 1;
					for (int k=1;k<=drink_max;k++){
						//System.out.printf("%s\t%s\t%s\n",before_processing[j],result[j],test[j][test[j].length-1]);
						if(result[j].equals("Drink")){
							count++;
							if (continue_flag > 1)
								continue_flag = -1;
						}
						else{
							if (continue_flag != -1)
								continue_flag++;
						}
						j++;
					}
					
					if (continue_flag >= 1 && count >= drink_min){
						//System.out.println("first");
						j = i;
						while (result[j].equals("Drink") && j<i+drink_max){
							j++;
						}
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					else if (continue_flag == -1 && count >= drink_max/2){
						//System.out.println("second");
						j = i;
						for(int k=1;k<=drink_max;k++){
							result[j] = "Drink";
							j++;
						}
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					else if ((continue_flag == -1 && count < drink_max/2) || (continue_flag >= 1 && count < drink_min)){
						//System.out.println("third");
						j = i;
						for(int k=1;k<=drink_max;k++){
							result[j] = "No_action";
							j++;
						}
						i = j;
						number_gap = number_gap + drink_max;
						continue;
					}
					/*
					System.out.println(count);
					if (count >= drink_min && count <= drink_max){//if the range belongs to this area
						System.out.println("first");
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					else if (count > drink_max){
						System.out.println("second");
						i = i + drink_max;
						number_gap = 0;
						
						int stop = i + count;****
						i = i + drink_max;****
						number_gap = 0;****
						while (i < stop){****
							result[i] = "No_action";****
							i++;****
							number_gap++;****
						}****
						time++;****
						continue;****
					}
					else{//if the range not belongs to this area
						System.out.println("third");
						while (i<j){
							result[i] = "No_action";
							i++;
						}
						number_gap = number_gap + count;
					}
					*/
				}
				else if (time > 0 && number_gap < gap_min){
					result[i] = "No_action";
					number_gap++;
					i++;
				}
				else if (time > 0 && number_gap >= gap_min){
					int j = i;
					int count = 0;
					
					int continue_flag = 1;
					for (int k=1;k<=drink_max && j < result.length;k++){
						if(result[j].equals("Drink")){
							count++;
							if (continue_flag > 1)
								continue_flag = -1;
						}
						else{
							if (continue_flag != -1)
								continue_flag++;
						}
						j++;
					}
					
					if (continue_flag >= 1 && count >= drink_min){
						//System.out.println("1111");
						j = i;
						while (j < result.length && result[j].equals("Drink") && j < i + drink_max){
							j++;
						}
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					
					else if (continue_flag == -1 && count >= drink_max/2){
						//System.out.println("2222");
						j = i;
						for(int k=1;k<=drink_max;k++){
							result[j] = "Drink";
							j++;
						}
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					else if ((continue_flag == -1 && count < drink_max/2) || (continue_flag >= 1 && count < drink_min)){
						//System.out.println("3333");
						j = i;
						for(int k=1;k<=drink_max && j<result.length;k++){
							result[j] = "No_action";
							j++;
						}
						i = j;
						number_gap = number_gap + drink_max;
						continue;
					}
					
					/*
					while (j < result.length && result[j].equals("Drink")){
						//System.out.printf("%s\t%s\t%s\n",before_processing[j],result[j],test[j][test[j].length-1]);
						count++;
						j++;
					}
					System.out.println(count);
					if (count >= drink_min && count <= drink_max){//if the range belongs to this area
						System.out.println("first");
						i = j;
						time++;
						number_gap = 0;
						continue;
					}
					else if (count > drink_max){
						System.out.println("second");
						i = i + drink_max;
						number_gap = 0;
						
						//int stop = i + count;
						//i = i + drink_max;
						//number_gap = 0;
						//while (i < stop){
						//	result[i] = "No_action";
						//	i++;
						//	number_gap++;
						//}
						//time++;
						//continue;
					}
					else{//if the range not belongs to this area
						System.out.println("third");
						while (i<j){
							result[i] = "No_action";
							i++;
						}
						number_gap = number_gap + count;
					}
					*/
				}
			}
			///////////////////////////////////////////////////////////
			else{
				//System.out.printf("%s\t%s\t%s\n",before_processing[i],result[i],test[i][test[i].length-1]);
				number_gap++;
				i++;
			}
		}
	}


	private static void human_logical_sequence(String[] result, String[][] test, String[] before_processing) {
		System.out.println("before processing\tafter processing\tactural value");
		int i=0;
		while (i<result.length){
			if (result[i].equals("Slap_head")){
				for (int j=i-1;j>=i-10;j--)
					result[j] = "No_action";
				for (int j=i+1;j<=i+10;j++){
					if (j >= result.length)
						break;
					result[j] = "No_action";
				}
				i=i+11;
				continue;
			}
			i++;
		}
		i=0;
		while (i<result.length){
			if (result[i].equals("Drink")){
				if (i>0 && result[i-1].equals("Drink")){
					result[i] = "No_action";
					i++;
					continue;
				}
				
				else{
					int number_of_drink = 0;
					for (int j=i+1;j<=i+3;j++)
						if (result[j].equals("Drink"))
							number_of_drink++;
					if (number_of_drink>=2){
						for (int j=i+1;j<=i+3;j++)
							result[j] = "Drink";
					}
					else{
						for (int j=i;j<=i+3;j++)
							result[j] = "No_action";
					}
					i = i+4;
					continue;
				}
			}
			i++;
		}
		for (int j=0;j<result.length;j++)
			System.out.printf("%s\t%s\t%s\n",before_processing[j],result[j],test[j][test[j].length-1]);
	}


	private static void Trees_prediction(Tree_node[] root, String[][] test,
			String[] result, String[] Class_type, ArrayList<Attribute_list> attribute_list, int [][]confidence) {
		for (int i = 0;i<test.length;i++){
			int vote [] = new int [Class_type.length];
			for (int j=0;j<root.length;j++){
				String value = Predict(root[j], test[i], Class_type);
				for (int k=0;k<Class_type.length;k++){
					if (value.equals(Class_type[k])){
						vote[k]++;
						break;
					}
				}
			}
			int max = vote[0];
			int index = 0;
			for (int j = 0;j<vote.length;j++){
				//System.out.print(vote[j]+"\t");
				if (max < vote[j]){
					max = vote[j];
					index = j;
				}
			}
			result[i] = Class_type[index];
			for (int k=0; k<Class_type.length; k++)
				confidence[i][k] = vote[k];
			
			//double percentage = (double)max/root.length;
			//if (percentage < 0.5)
			//	result[i] = "No_action";
			//confidence[i] = percentage;
			/*
			int second_max = -1;//this is 10 times vote
			for (int j = 0;j<vote.length;j++)
				if (j != index)
					if (second_max < vote[j])
						second_max = vote[j];
				
			if (second_max == 0 || max/second_max >= 10)
				result[i] = Class_type[index];
			else
				result[i] = "No_action";
			*/
			
			
			
			
			/*if (!result[i].equals(test[i][test[i].length-1])){
				not_match++;
				System.out.print("\tnot match");
			}*/
			//System.out.println();
		}
	}


	private static void Print_tree(Tree_node root, String str1,
			String str2) {
		if (!root.tpye.equals(" ")){
			System.out.print(str1+root.tpye+str2+" (");
			int i=0;
			for (;i<root.class_compare.length-1;i++)
				System.out.print(root.class_compare[i]+":");
			System.out.println(root.class_compare[i]+")");
		}
		else{
			if (root.continuous == 0){
				System.out.println(str1+"[ "+root.attribute+" ]"+str2);
				str1 = str1 + "----";
				for (int i=1;i<=root.range;i++){
					str2 = " : "+root.values[i];
					Print_tree(root.value_range[i],str1,str2);
				}
			}
			else{
				System.out.println(str1+"[ "+root.attribute+" ]"+str2);
				str1 = str1 + "----";
				str2 = " <= "+root.thresholds;
				Print_tree(root.small,str1,str2);
				str2 = " > "+root.thresholds;
				Print_tree(root.large,str1,str2);
			}
			
		}
	}
	
	private static String Predict(Tree_node root, String test[], String Class_type []) {
		if (!root.tpye.equals(" ")){
			return root.tpye;
		}
		else{
			if (root.continuous == 0){
				int index = root.index;//find out which attribute it is
				String value = test[index];//find out what the value it is for the test set
				for (int i=1;i<=root.range;i++){
					if (value.equals(root.values[i])){
						
						return Predict(root.value_range[i],test,Class_type);
					}
				}
			}
			else{
				int index = root.index;//find out which attribute it is
				double value = Double.parseDouble(test[index]);
				if (value<=root.thresholds)
					return Predict(root.small,test,Class_type);
				else
					return Predict(root.large,test,Class_type);
			}
		}
		return Class_type[0];
	}
	
	private static void Build_tree(Tree_node root,
			ArrayList<Attribute_list> attribute_list, String[][] train,
			int examples, int attribute_number, int continuous_attribute_number, int[] attribute_index, int current_attribute_number,
			String[] class_type,int[] continuous_attribute_index,
			ArrayList<Continuous_Attribute_list> continuous_attribute_list, String default_type,int current_level) {
		
		
		int Count_type_number[] = new int [class_type.length];
		for (int i=0;i<class_type.length;i++)
			Count_type_number[i] = 0;
		
		for (int i=0;i<examples;i++){
			int lenth = train[i].length;
			for (int j=0;j<class_type.length;j++){
				if (train[i][lenth-1].equals(class_type[j])){
					Count_type_number[j]++;
					break;
				}
			}
		}
		
		if (examples == 0){//examples are 0,first way to stop building the tree
			//System.out.println("examples is 000000000:  "+default_type);
			root.tpye = default_type;
			
			root.class_compare = new int [class_type.length];
			for (int i=0;i<class_type.length;i++)
				root.class_compare[i] = Count_type_number[i];
			//root.Prior_MDL = 0;
			//System.out.println(str1+root.tpye+str2);
			return;
		}
		
		//the second way to stop building a tree.
		for (int i=0;i<class_type.length;i++){
			if (Count_type_number[i] == examples){
				root.tpye = class_type[i];
				root.class_compare = new int [class_type.length];
				for (int j=0;j<class_type.length;j++)
					root.class_compare[j] = Count_type_number[j];
				//root.Prior_MDL = Prior_MDL(examples,Count_type_number);
				//System.out.println(str1+root.tpye+str2);
				return;
			}
		}
		//the third way to stop building a tree.
		int max = 0;
		int mark_max = 0;
		for (int i=0;i<class_type.length;i++){//find out the majority of the current examples
			if (Count_type_number[i]>=max){
				max = Count_type_number[i];
				mark_max = i;
			}
		}
		default_type = class_type[mark_max];
		if (current_attribute_number == 0){//means no more attributes,then return the majority as the value
			root.tpye = class_type[mark_max];
			
			root.class_compare = new int [class_type.length];
			for (int i=0;i<class_type.length;i++)
				root.class_compare[i] = Count_type_number[i];
			//root.Prior_MDL = Prior_MDL(examples,Count_type_number);
			//System.out.println(str1+root.tpye+str2);
			return;
		}
		
		//if the first way and second way don't match,then continue to build a tree.
		double Probablity [] = new double [class_type.length];
		double Info_before = 0.0;
		int flag=0;
		for (int i=0;i<class_type.length;i++){
			if (Count_type_number[i] == examples){
				Info_before = 0.0;
				flag = 1;
				break;
			}
		}
		if (flag == 0){
			for (int i=0;i<class_type.length;i++)
				Probablity[i] = (double) Count_type_number[i]/examples;
			for (int i=0;i<class_type.length;i++){
				if (Count_type_number[i] == 0)
					Info_before = Info_before;
				else
					Info_before = -Probablity[i]*(Math.log(Probablity[i])/Math.log(2)) + Info_before;
			}
		}
		double infomation_gain[] = new double [attribute_number];
		for (int i=0;i<attribute_number;i++)
			infomation_gain[i] = -3.0;
		
		double continuous_info_gain[] = new double [continuous_attribute_number];
		for (int i=0;i<continuous_attribute_number;i++)
			continuous_info_gain[i] = -3.0;
		
		for (int i=0;i<attribute_number;i++){//try all the attributes
			if (attribute_index[i] == 1){//means this attribute is currently available
				int value_range = attribute_list.get(i).attribute_range.length - 1;
				
				double part_infor_gain [] = new double [value_range+1];
				double info_after = 0.0;
				for (int j=1;j<=value_range;j++){//for each value of the current attribute
					
					int count=0;//how many examples in the specific value
					int sub_tpye_number[] = new int [class_type.length];
					for (int k=0;k<class_type.length;k++)//creat the sub
						sub_tpye_number[k] = 0;
					
					for (int k=0;k<examples;k++){//test each example
						if (train[k][i].equals(attribute_list.get(i).attribute_range[j])){
							count++;
							for (int m =0;m<class_type.length;m++){
								if (train[k][train[k].length-1].equals(class_type[m])){
									sub_tpye_number[m]++;
								}
							}
						}
						
					}
					double info = 0;
					int stop = 0;
					for (int k=0;k<class_type.length;k++){
						if (sub_tpye_number[k] == count){
							info = 0.0;
							stop = 1;
							break;
						}
					}
					if (stop == 0){
						double probablit [] = new double [class_type.length];
						for (int k=0;k<class_type.length;k++)
							probablit[k] = (double) sub_tpye_number[k]/count;
						
						for (int k=0;k<class_type.length;k++){
							if (sub_tpye_number[k] == 0)
								info = info;
							else
								info = -probablit[k]*(Math.log(probablit[k])/Math.log(2)) + info;
						}
					}
					part_infor_gain[j] = info*count/examples;
				}
				for (int j =1;j<=value_range;j++){//summary all the branch information together to get the "after information"
					info_after = info_after + part_infor_gain[j];
				}
				infomation_gain[i] = Info_before - info_after;
			}
		}
		
		for (int i=0;i<continuous_attribute_number;i++){//try all the continuous_attributes
			if (continuous_attribute_index[i] == 1){//means this attribute is currently available
				double thresholds = continuous_attribute_list.get(i).thresholds;
				
				double part_infor_gain_small_equal;
				double part_infor_gain_large;
				double info_after = 0.0;
				
				int small_count=0;//how many examples in the specific value
				int large_count=0;
				int small_sub_tpye_number[] = new int [class_type.length];
				int large_sub_tpye_number[] = new int [class_type.length];
				
				for (int k=0;k<class_type.length;k++)//create the sub
					small_sub_tpye_number[k] = 0;
				for (int k=0;k<class_type.length;k++)//create the sub
					large_sub_tpye_number[k] = 0;
					
				int index = continuous_attribute_list.get(i).position;
				for (int k=0;k<examples;k++){//test each example
					double current_value = Double.parseDouble(train[k][index]);
					if (current_value<=thresholds){
						small_count++;
						for (int m =0;m<class_type.length;m++){
							if (train[k][train[k].length-1].equals(class_type[m])){
								small_sub_tpye_number[m]++;
							}
						}
					}
					else if (current_value>thresholds){
						large_count++;
						for (int m =0;m<class_type.length;m++){
							if (train[k][train[k].length-1].equals(class_type[m])){
								large_sub_tpye_number[m]++;
							}
						}
					}
					
				}
				double small_info = 0;
				int stop = 0;
				for (int k=0;k<class_type.length;k++){
					if (small_sub_tpye_number[k] == small_count){
						small_info = 0.0;
						stop = 1;
						break;
					}
				}
				if (stop == 0){
					double probablit [] = new double [class_type.length];
					for (int k=0;k<class_type.length;k++)
						probablit[k] = (double) small_sub_tpye_number[k]/small_count;
						
					for (int k=0;k<class_type.length;k++){
						if (small_sub_tpye_number[k] == 0)
							small_info = small_info;
						else
							small_info = -probablit[k]*(Math.log(probablit[k])/Math.log(2)) + small_info;
					}
				}
				part_infor_gain_small_equal = small_info*small_count/examples;
				
				double large_info = 0;
				int stop_1 = 0;
				for (int k=0;k<class_type.length;k++){
					if (large_sub_tpye_number[k] == large_count){
						large_info = 0.0;
						stop_1 = 1;
						break;
					}
				}
				if (stop_1 == 0){
					double probablit [] = new double [class_type.length];
					for (int k=0;k<class_type.length;k++)
						probablit[k] = (double) large_sub_tpye_number[k]/large_count;
						
					for (int k=0;k<class_type.length;k++){
						if (large_sub_tpye_number[k] == 0)
							large_info = large_info;
						else
							large_info = -probablit[k]*(Math.log(probablit[k])/Math.log(2)) + large_info;
					}
				}
				part_infor_gain_large = large_info*large_count/examples;
				info_after = part_infor_gain_large + part_infor_gain_small_equal;
				continuous_info_gain[i] = Info_before - info_after;
			}
		}
		
		double temp_1 = -3.0;
		int mark_1 = 0;//"mark" will be the index in the "attribute_list" 
		for (int i=0;i<attribute_number;i++){//choosing the best attribute which has the highest information gain.
			if (attribute_index[i] == 1){
				if (infomation_gain[i]>=temp_1){
					temp_1 = infomation_gain[i];
					mark_1 = i;
				}
			}
		}
		
		double temp_2 = -3.0;
		int mark_2 = 0;//"mark" will be the index in the "attribute_list" 
		for (int i=0;i<continuous_attribute_number;i++){//choosing the best attribute which has the highest information gain.
			if (continuous_attribute_index[i] == 1){
				if (continuous_info_gain[i]>=temp_2){
					temp_2 = continuous_info_gain[i];
					mark_2 = i;
				}
			}
		}
		
		if (temp_1>temp_2){
			current_attribute_number = current_attribute_number - 1;//reduce the available attributes
			attribute_index[mark_1] = 0;//take off one attribute from the "attribute_list".
			
			root.tpye = " ";
			root.level = current_level;
			if (max_level < current_level)
				max_level = current_level;
			
			root.default_type = default_type;
			//root.Prior_MDL = Prior_MDL(examples,Count_type_number);
			root.attribute = attribute_list.get(mark_1).attribute_range[0];//name of the attribute
			root.index = mark_1;////////////////
			root.continuous = 0;
			//System.out.println(str1+"[ "+root.attribute+" ]"+str2);
			//str1 = str1 + "----";
			
			root.range = attribute_list.get(mark_1).attribute_range.length - 1;//how many different value of the current attribute
			root.values = new String [root.range+1];//store the different value of the attribute
			for (int i=1;i<=root.range;i++)
				root.values[i] = attribute_list.get(mark_1).attribute_range[i];
			
			root.value_range = new Tree_node[root.range+1];
			for (int i=0;i <= root.range;i++)
				root.value_range[i] = new Tree_node();
			
			current_level = current_level + 1;
			for (int i=1;i <= root.range;i++){//this loop is for each value of the attribute,and extend it to be sub-tree
				String temp_examples [][];
				String value;
				value = root.values[i];
				int number = 0;//count how many examples have such a value in this attribute
				for (int j=0;j<examples;j++)
					if (value.equals(train[j][mark_1]))
						number++;
				temp_examples = new String [number][];//the temp_examples which have such a value in this attribute
				for (int j=0,k=0;j<examples;j++){//abstract some examples,and transfer them to the next level
					if (value.equals(train[j][mark_1])){
						temp_examples[k] = train[j];
						k++;
					}
				}
				//str2 = " : "+root.values[i];
				int temp_attribute_index [] = new int [attribute_number];
				for (int j=0;j<attribute_number;j++)
					temp_attribute_index[j] = attribute_index[j];
				int temp_current_attribute_number = current_attribute_number;
				
				int temp_con_attribute_index [] = new int [continuous_attribute_number];
				for (int j=0;j<continuous_attribute_number;j++)
					temp_con_attribute_index[j] = continuous_attribute_index[j];
				
				
				Build_tree(root.value_range[i],attribute_list,temp_examples,number,attribute_number,continuous_attribute_number,
						temp_attribute_index,temp_current_attribute_number,
							class_type,temp_con_attribute_index,continuous_attribute_list, default_type,current_level);
			}
		}
		if (temp_1<=temp_2){
			current_attribute_number = current_attribute_number - 1;//reduce the available attributes
			continuous_attribute_index[mark_2] = 0;//take off one attribute from the "attribute_list".
			
			root.tpye = " ";
			root.level = current_level;
			if (max_level < current_level)
				max_level = current_level;
			
			root.default_type = default_type;
			//root.Prior_MDL = Prior_MDL(examples,Count_type_number);
			root.continuous = 1;
			root.attribute = continuous_attribute_list.get(mark_2).name;//name of the attribute
			root.index = continuous_attribute_list.get(mark_2).position;////////////////
			root.thresholds = continuous_attribute_list.get(mark_2).thresholds;
			//System.out.println(str1+"[ "+root.attribute+" ]"+str2);
			//str1 = str1 + "----";
			
			root.small = new Tree_node();
			root.large = new Tree_node();
			
				String small_examples [][];
				String large_examples [][];
				
				int small_number = 0;//count how many examples have such a value in this attribute
				int large_number = 0;
				for (int j=0;j<examples;j++){
					double value = Double.parseDouble(train[j][root.index]);
					if (value <= root.thresholds)
						small_number++;
					else
						large_number++;
				}
				small_examples = new String [small_number][];//the temp_examples which have such a value in this attribute
				large_examples = new String [large_number][];//the temp_examples which have such a value in this attribute
				for (int j=0,k=0,m=0;j<examples;j++){//abstract some examples,and transfer them to the next level
					double value = Double.parseDouble(train[j][root.index]);
					if (value <= root.thresholds){
						small_examples[k] = train[j];
						k++;
					}
					else{
						large_examples[m] = train[j];
						m++;
					}
				}
				
				//str2 = " <= "+root.thresholds;
				int temp_attribute_index [] = new int [attribute_number];
				for (int j=0;j<attribute_number;j++)
					temp_attribute_index[j] = attribute_index[j];
				int temp_current_attribute_number = current_attribute_number;
				
				int temp_con_attribute_index [] = new int [continuous_attribute_number];
				for (int j=0;j<continuous_attribute_number;j++)
					temp_con_attribute_index[j] = continuous_attribute_index[j];
				
				current_level = current_level + 1;
				Build_tree(root.small,attribute_list,small_examples,small_number,attribute_number,continuous_attribute_number,
						temp_attribute_index,temp_current_attribute_number,
							class_type, temp_con_attribute_index,continuous_attribute_list, default_type,current_level);
				
				//str2 = " > "+root.thresholds;
				Build_tree(root.large,attribute_list,large_examples,large_number,attribute_number,continuous_attribute_number,
						attribute_index,temp_current_attribute_number,
							class_type, continuous_attribute_index,continuous_attribute_list, default_type,current_level);
		}
	}
}
