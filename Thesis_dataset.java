import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class Thesis_dataset {
	static int window_size = 50;
	
	//static double drinking [][] = {{299.87,449.08,598.37,899.08,1198.48},
	//							   {301.88,450.98,600.98,900.48,1200.38}};
	//static double slap_head [][] = {{79.47,125.10,271.57,316.68,521.48,659.37,731.68,1334.08,1454.08,1619.63},
	//								{79.90,125.49,272.08,317.16,521.89,659.83,732.10,1334.52,1454.58,1620.13}};
	
	
	static double drinking [][] = {{69.82,80.06,89.85,100.32,110.23,120.22,140.2,160.4,180.2,209.68,211.98,215.28,218.98,222.29,225.64,229.19,232.88,/*  */236.14,239.39/*  */,270.19,272.5,274.4,276.1,277.9,279.69,281.4,283.49,285.39,287.29,289.2},
		   						   {73.2,83.12,93.1,103.25,113.07,123.04,143.23,163.43,183.23,210.58,213.43,216.86,220.29,223.54,226.99,230.89,234.29,/*  */237.39,240.39/*  */,272.49,274.39,276.09,277.89,279.68,281.39,283.48,285.38,287.28,289.19,291.39}};
	static double slap_head [][] = {{10.25,20.19,30.23,40.01,50.23,60.2,130.18,150.25,170.26,190.15,190.90,191.6,192.4,193.2,193.91,194.69,195.4,196.1,196.9,250.25,251.19,252.24,253.45,254.45,255.65,256.7,257.66,258.8,259.8},
									{10.60,20.48,30.51,40.35,50.51,60.53,130.45,150.54,170.55,190.51,191.20,191.9,192.65,193.45,194.2,194.95,195.6,196.38,197.08,250.46,251.51,252.6,253.71,254.75,255.88,256.9,258,259.1,260.11}};
	
	
	static class WINDOW{
		double data[][];
		double mean_value[];
		double max_min_gap[];
		double RMS[];
		double standard_deviation[];
		double linear_regression[];
		double Pearson_Acc_Gyro[];//3 axies relations
		String Class_type;
		//double DFT[];
	}
	public static void main(String args[]) throws IOException{
		String filename;
		filename = "E:/Machine Learning/thesis/new_data_set.txt";
		FileReader fr = new FileReader(filename);
		BufferedReader br =  new BufferedReader(fr);
		
		String Line;
		String original_array[][] = new String[200000][];
		int total_record_number = 0;
		while ((Line = br.readLine()) != null){
			original_array[total_record_number] = Line.split("\t");
			total_record_number++;
		}
		
		int number_of_windows = total_record_number/window_size;
		System.out.println(total_record_number);
		//System.out.println(original_array[0][1]);
		//System.out.println(original_array[31061][1]);
		WINDOW windows[] = new WINDOW [number_of_windows];
		for (int i=0;i<number_of_windows;i++){
			windows[i] = new WINDOW();
			windows[i].data = new double [7][50];
			windows[i].mean_value = new double [7];
			windows[i].max_min_gap = new double [7];
			windows[i].RMS = new double [7];
			windows[i].standard_deviation = new double [7];
			windows[i].linear_regression = new double [7];
			windows[i].Pearson_Acc_Gyro = new double [6];//later maybe add the vector relationship
		}
		int windows_index = -1;
		int inside_index = 0;
		//System.out.println(original_array[2410][1]);
		
		for (int i=0;i<total_record_number;i++){
			if (i%window_size == 0){
				windows_index++;
				inside_index = 0;
				if (windows_index == number_of_windows)
					break;
			}
			
			windows[windows_index].data[0][inside_index] = Double.parseDouble(original_array[i][1]);//store the time stamp
			
			windows[windows_index].data[1][inside_index] = Double.parseDouble(original_array[i][3]);//store the Gyro x
			windows[windows_index].data[2][inside_index] = Double.parseDouble(original_array[i][4]);//store the Gyro y
			windows[windows_index].data[3][inside_index] = Double.parseDouble(original_array[i][5]);//store the Gyro z
			windows[windows_index].data[4][inside_index] = Double.parseDouble(original_array[i][6]);//store the LinAcc x
			windows[windows_index].data[5][inside_index] = Double.parseDouble(original_array[i][7]);//store the LinAcc x
			windows[windows_index].data[6][inside_index] = Double.parseDouble(original_array[i][8]);//store the LinAcc x
			inside_index++;
		}
		DecimalFormat df = new DecimalFormat("###.000");
		
		int mark = 1;
		for (int i=0;i<number_of_windows;i++){
			for (int j=1;j<7;j++)
				windows[i].mean_value[j] = Double.parseDouble( df.format( Caculate_mean(windows[i].data[j]) ) );
			for (int j=1;j<7;j++)
				windows[i].max_min_gap[j] = Double.parseDouble( df.format( Caculate_max_min_gap(windows[i].data[j]) ) );
			for (int j=1;j<7;j++)
				windows[i].RMS[j] = Double.parseDouble( df.format( Caculate_RMS(windows[i].data[j]) ) );
			for (int j=1;j<7;j++)
				windows[i].standard_deviation[j] = Double.parseDouble( df.format( Caculate_standard_deviation(windows[i].data[j], windows[i].mean_value[j]) ) );
			for (int j=1;j<7;j++)
				windows[i].linear_regression[j] = Double.parseDouble( df.format( regression(windows[i].data[j]) ) );
			
			windows[i].Pearson_Acc_Gyro[0] = Pearson_factor(windows[i].data[1], windows[i].data[4],
											windows[i].standard_deviation[1], windows[i].standard_deviation[4],
											windows[i].mean_value[1], windows[i].mean_value[4]);
			windows[i].Pearson_Acc_Gyro[0] = Double.parseDouble( df.format(windows[i].Pearson_Acc_Gyro[0]) );
			
			windows[i].Pearson_Acc_Gyro[1] = Pearson_factor(windows[i].data[2], windows[i].data[5],
											windows[i].standard_deviation[2], windows[i].standard_deviation[5],
											windows[i].mean_value[2], windows[i].mean_value[5]);
			windows[i].Pearson_Acc_Gyro[1] = Double.parseDouble( df.format(windows[i].Pearson_Acc_Gyro[1]) );
			
			windows[i].Pearson_Acc_Gyro[2] = Pearson_factor(windows[i].data[3], windows[i].data[6],
											windows[i].standard_deviation[3], windows[i].standard_deviation[6],
											windows[i].mean_value[3], windows[i].mean_value[6]);
			windows[i].Pearson_Acc_Gyro[2] = Double.parseDouble( df.format(windows[i].Pearson_Acc_Gyro[2]) );
			
			windows[i].Pearson_Acc_Gyro[3] = Pearson_factor(windows[i].data[4], windows[i].data[5],
											windows[i].standard_deviation[4], windows[i].standard_deviation[5],
											windows[i].mean_value[4], windows[i].mean_value[5]);
			windows[i].Pearson_Acc_Gyro[3] = Double.parseDouble( df.format(windows[i].Pearson_Acc_Gyro[3]) );
			
			windows[i].Pearson_Acc_Gyro[4] = Pearson_factor(windows[i].data[5], windows[i].data[6],
											windows[i].standard_deviation[5], windows[i].standard_deviation[6],
											windows[i].mean_value[5], windows[i].mean_value[6]);
			windows[i].Pearson_Acc_Gyro[4] = Double.parseDouble( df.format(windows[i].Pearson_Acc_Gyro[4]) );
			
			windows[i].Pearson_Acc_Gyro[5] = Pearson_factor(windows[i].data[4], windows[i].data[6],
											windows[i].standard_deviation[4], windows[i].standard_deviation[6],
											windows[i].mean_value[4], windows[i].mean_value[6]);
			windows[i].Pearson_Acc_Gyro[5] = Double.parseDouble( df.format(windows[i].Pearson_Acc_Gyro[5]) );
			
			
			int number_of_drink = 0;
			int number_of_slap = 0;
			int label_flag = 0;
			for (int j=0;j<drinking[0].length;j++){
				number_of_drink = 0;
				for (int k=0;k<windows[i].data[0].length;k++){
					if (windows[i].data[0][k] >= drinking[0][j] && windows[i].data[0][k] <= drinking[1][j])
						number_of_drink++;
				}
				if (number_of_drink > 40){
					windows[i].Class_type = "Drink";
					label_flag = 1;
					break;
				}
			}
			if (label_flag == 1){
				System.out.println(windows[i].Pearson_Acc_Gyro[0]+" "+windows[i].Pearson_Acc_Gyro[1]+" "+windows[i].Pearson_Acc_Gyro[2]);
				System.out.println(windows[i].Class_type+"\t \t"+mark);
				mark++;
				continue;
			}
				
			for (int j=0;j<slap_head[0].length;j++){
				number_of_slap = 0;
				for (int k=0;k<windows[i].data[0].length;k++){
					if (windows[i].data[0][k] >= slap_head[0][j] && windows[i].data[0][k] <= slap_head[1][j])
						number_of_slap++;
				}
				if (number_of_slap > 20){
					windows[i].Class_type = "Slap_head";
					label_flag = 1;
					break;
				}
			}
			if (label_flag == 1){
				System.out.println(windows[i].Pearson_Acc_Gyro[0]+" "+windows[i].Pearson_Acc_Gyro[1]+" "+windows[i].Pearson_Acc_Gyro[2]);
				System.out.println(windows[i].Class_type+"\t \t"+mark);
				mark++;
				continue;
			}
			else
				windows[i].Class_type = "No_action";
		}
		
		File file = new File("E:/Machine Learning/thesis/new_thesis_data.txt");
		FileWriter fw = new FileWriter(file);
		
		File file_time = new File("E:/Machine Learning/thesis/time_stample.txt");
		FileWriter fw_time = new FileWriter(file_time);
		
		for (int i=3;i<number_of_windows;i++){
			
			for (int k=i-3;k<=i;k++){
				for (int j=1;j<7;j++)
					fw.write(windows[k].mean_value[j]+" "+windows[k].max_min_gap[j]+" "+windows[k].RMS[j]+
							" "+windows[k].standard_deviation[j]+" "+windows[k].linear_regression[j]+" ");
				for (int j=0;j<6;j++)
					fw.write(windows[k].Pearson_Acc_Gyro[j]+" ");
			}
			
			fw.write(windows[i].Class_type+"\n");
		}
		fw.flush();
	    fw.close();
	}
	
	private static double Pearson_factor(double[] array1, double[] array2, double dev_1, double dev_2, double mean_1, double mean_2) {
		double sum = 0;
		for (int i=0;i<array1.length;i++)
			sum = sum + (array1[i] - mean_1) * (array2[i] - mean_2);
		sum = sum / array1.length;// covariance
		sum = sum / (dev_1 * dev_2);
		return sum;
	}
	
	private static double regression(double[] array) {
		double w0=1, w1=1;
		
		double previous_error = 0;
		while (true){
			
			double total_error = 0;
			for (int i=0;i<array.length;i++){
				double error = (array[i] - (w0+w1*i));
				
				w0 = w0 + 0.0001*error;
				w1 = w1 + 0.0001*error*i;
				
				total_error = total_error + Math.abs(error);
			}
			if (Math.abs(total_error) - previous_error < 0.001)
				break;
			else
				previous_error = Math.abs(total_error);
		}
		return w1;
	}
	
	private static double Caculate_standard_deviation(double[] array, double mean) {
		double sum = 0;
		for (int i=0;i<array.length;i++)
			sum = sum + (array[i] - mean) * (array[i] - mean);
		sum = sum / array.length;
		sum = Math.sqrt(sum);
		return sum;
	}
	
	private static double Caculate_RMS(double[] array) {
		double sum = 0;
		for (int i=0;i<array.length;i++)
			sum = sum + array[i] * array[i];
		sum = sum / array.length;
		sum = Math.sqrt(sum);
		return sum;
	}
	
	private static double Caculate_max_min_gap(double[] array) {
		double max = array[0];
		double min = array[0];
		
		for (int i=1;i<array.length;i++){
			if (array[i]>max)
				max = array[i];
			if (array[i]<min)
				min = array[i];
		}
		return max - min;
	}
	
	private static double Caculate_mean(double[] array) {
		double sum = 0;
		for(int i=0;i<array.length;i++)
			sum = sum + array[i];
		sum = sum/array.length;
		return sum;
	}
}
