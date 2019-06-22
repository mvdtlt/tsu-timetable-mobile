package com.tinyshade.timetable;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {
    private static int currentWeek;
    public static List<String> mList;
    public static Weektt weektt = new Weektt();
    private ArrayAdapter<String> instListAdapter;
    private ArrayAdapter<String> mAutoCompleteAdapter;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private class getGroupList extends AsyncTask<String, Void, Void> {
        private getGroupList() {
        }

        /* synthetic */ getGroupList(MainActivity x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: protected */
        public void onPreExecute() {
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(String... params) {
            String url = "";
            try {
                Elements hrefs = Jsoup.connect("http://tt.binarus.ru/php/ttgr.php?inst=" + params[0]).get().getElementsByTag("a");
                Log.d("siz", params[0]);
                Iterator it = hrefs.iterator();
                while (it.hasNext()) {
                    MainActivity.mList.add(((Element) it.next()).text());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Void result) {
            MainActivity.this.mAutoCompleteAdapter = new ArrayAdapter(MainActivity.this, 17367050, MainActivity.mList);
            ((Spinner) MainActivity.this.findViewById(R.id.gr)).setAdapter(MainActivity.this.mAutoCompleteAdapter);
            ((Spinner) MainActivity.this.findViewById(R.id.gr)).setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
                    MainActivity.this.startTask(selectedItemView);
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            int spinnerValueGr = MainActivity.this.getSharedPreferences("SpinnerPosition", 0).getInt("userChoiceSpinnerGr", -1);
            if (spinnerValueGr != -1 && spinnerValueGr <= MainActivity.mList.size()) {
                ((Spinner) MainActivity.this.findViewById(R.id.gr)).setSelection(spinnerValueGr);
            }
        }
    }

    private class getTT extends AsyncTask<String, Void, Void> {
        private getTT() {
        }

        /* synthetic */ getTT(MainActivity x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: protected */
        public void onPreExecute() {
            ((ProgressBar) MainActivity.this.findViewById(R.id.progressBar)).setVisibility(0);
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(String... params) {
            MainActivity.weektt = new Weektt();
            String url = "";
            if (params[2].equals("0")) {
                url = "http://tt.binarus.ru/php/opentt.php?inst=" + params[0] + "&grup=" + params[1];
            }
            if (!params[2].equals("0")) {
                url = "http://tt.binarus.ru/php/opentt.php?inst=" + params[0] + "&grup=" + params[1] + "&ned=" + params[2];
            }
            try {
                Document doc = Jsoup.connect(url).get();
                if (doc.toString().contains("Расписание для этой группы/преподавателя отсутствует")) {
                    MainActivity.weektt.mon.add("Расписание не найдено (нет пар).");
                    MainActivity.weektt.mon.add("Если вы уверены, что пары есть - проверьте правильность данных");
                    if (params[2].equals("0")) {
                        MainActivity.weektt.mon.add("Институт/кафедра: " + params[0] + ". Группа/Преподаватель: " + params[1]);
                    } else {
                        MainActivity.weektt.mon.add("Институт/кафедра: " + params[0] + ". Группа/Преподаватель: " + params[1] + ". Неделя: " + params[2]);
                    }
                    MainActivity.weektt.mon.add("Вы можете отправить сообщение об ошибке в программе на mvd@decenter.tech");
                    MainActivity.weektt.tue = MainActivity.weektt.mon;
                    MainActivity.weektt.wed = MainActivity.weektt.mon;
                    MainActivity.weektt.thu = MainActivity.weektt.mon;
                    MainActivity.weektt.fri = MainActivity.weektt.mon;
                    MainActivity.weektt.sat = MainActivity.weektt.mon;
                } else {
                    Elements trs = doc.getElementsByTag("tr");
                    if (trs.isEmpty()) {
                        MainActivity.weektt.mon.add("Расписание не найдено. Проверьте правильность введенных данных");
                        MainActivity.weektt.mon.add("Институт: " + params[0] + ". Группа: " + params[1] + ". Неделя:" + params[2]);
                        MainActivity.weektt.mon.add("Если все в порядке, возможно, расписания еще нет на сайте.");
                        MainActivity.weektt.tue = MainActivity.weektt.mon;
                        MainActivity.weektt.wed = MainActivity.weektt.mon;
                        MainActivity.weektt.thu = MainActivity.weektt.mon;
                        MainActivity.weektt.fri = MainActivity.weektt.mon;
                        MainActivity.weektt.sat = MainActivity.weektt.mon;
                    } else {
                        MainActivity.weektt.mon = getDay(trs, 0);
                        MainActivity.weektt.tue = getDay(trs, 1);
                        MainActivity.weektt.wed = getDay(trs, 2);
                        MainActivity.weektt.thu = getDay(trs, 3);
                        MainActivity.weektt.fri = getDay(trs, 4);
                        MainActivity.weektt.sat = getDay(trs, 5);
                        String test = ((Element) ((Element) trs.get(0)).select("td").get(1)).toString();
                        int t = test.indexOf("№") + 1;
                        MainActivity.currentWeek = Integer.parseInt(test.substring(t, t + 2));
                        List<String> days = new ArrayList();
                        for (int i = 1; i < 7; i++) {
                            days.add(((Element) ((Element) trs.get(1)).select("td").get(i)).text().toString());
                        }
                        MainActivity.weektt.days = days;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /* Access modifiers changed, original: protected */
        public List<String> getDay(Elements tr, int weekday) {
            int para = 0;
            List<String> fullDay = new ArrayList();
            String day = "";
            for (int i = 1; i < tr.size(); i++) {
                if (((Element) ((Element) tr.get(i)).select("td").get(weekday + 1)).text().toString() != "") {
                    if (para != 0) {
                        day = (day + "Пара " + para + ": ") + ((Element) ((Element) tr.get(i)).select("td").get(weekday + 1)).text().toString();
                    }
                    if (day != "") {
                        fullDay.add(day);
                    }
                    day = "";
                }
                para++;
            }
            return fullDay;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Void result) {
            ((ProgressBar) MainActivity.this.findViewById(R.id.progressBar)).setVisibility(4);
            MainActivity.this.mSectionsPagerAdapter.notifyDataSetChanged();
            if (!MainActivity.weektt.days.isEmpty()) {
                MainActivity.this.findViewById(R.id.tabs).setVisibility(0);
                ((TextView) MainActivity.this.findViewById(R.id.week)).setText("Неделя №" + Integer.toString(MainActivity.currentWeek));
                MainActivity.this.mViewPager.setCurrentItem(Calendar.getInstance().get(7) - 2, true);
            }
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.tableLayout);
            for (String fullday : MainActivity.weektt.getDayFromNum(getArguments().getInt(ARG_SECTION_NUMBER))) {
                TableRow tableRow = new TableRow(getActivity());
                tableRow.setLayoutParams(new LayoutParams(-1, -1));
                TextView tv = new TextView(getActivity());
                tv.setLayoutParams(new TableRow.LayoutParams(-2, -2));
                tableRow.setBackgroundResource(R.drawable.ttbg);
                tv.setText(fullday);
                tv.setGravity(17);
                tableRow.addView(tv);
                tableLayout.addView(tableRow);
            }
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        public int getItemPosition(Object object) {
            return -2;
        }

        public int getCount() {
            return 6;
        }

        public CharSequence getPageTitle(int position) {
            if (MainActivity.weektt.days.isEmpty()) {
                return "";
            }
            switch (position) {
                case 0:
                    return (CharSequence) MainActivity.weektt.days.get(0);
                case 1:
                    return (CharSequence) MainActivity.weektt.days.get(1);
                case 2:
                    return (CharSequence) MainActivity.weektt.days.get(2);
                case 3:
                    return (CharSequence) MainActivity.weektt.days.get(3);
                case 4:
                    return (CharSequence) MainActivity.weektt.days.get(4);
                case 5:
                    return (CharSequence) MainActivity.weektt.days.get(5);
                default:
                    return null;
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        currentWeek = 0;
        ((Switch) findViewById(R.id.modeSwitch)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    MainActivity.this.instListAdapter = new ArrayAdapter(MainActivity.this, 17367050, MainActivity.this.getResources().getStringArray(R.array.chairs));
                } else {
                    MainActivity.this.instListAdapter = new ArrayAdapter(MainActivity.this, 17367050, MainActivity.this.getResources().getStringArray(R.array.insts));
                }
                ((Spinner) MainActivity.this.findViewById(R.id.inst)).setAdapter(MainActivity.this.instListAdapter);
                Editor prefEditor = MainActivity.this.getSharedPreferences("SpinnerPosition", 0).edit();
                prefEditor.putBoolean("SwitchPos", b);
                prefEditor.commit();
            }
        });
        ((TextView) findViewById(R.id.week)).setText("Неделя №" + Integer.toString(currentWeek));
        this.mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        this.mViewPager = (ViewPager) findViewById(R.id.container);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        ((TabLayout) findViewById(R.id.tabs)).setupWithViewPager(this.mViewPager);
        mList = new ArrayList();
        this.mAutoCompleteAdapter = new ArrayAdapter(this, 17367050, mList);
        ((Spinner) findViewById(R.id.gr)).setAdapter(this.mAutoCompleteAdapter);
        if (weektt.days.isEmpty()) {
            findViewById(R.id.tabs).setVisibility(4);
        }
        ((Spinner) findViewById(R.id.inst)).setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                MainActivity.mList.clear();
                String[] params = new String[]{MainActivity.this.instNameToUrlName(((Spinner) MainActivity.this.findViewById(R.id.inst)).getItemAtPosition(i).toString())};
                Log.d("GRP", MainActivity.this.instNameToUrlName("ИЭиЭ"));
                new getGroupList(MainActivity.this, null).execute(params);
                int userChoice = ((Spinner) MainActivity.this.findViewById(R.id.inst)).getSelectedItemPosition();
                Editor prefEditor = MainActivity.this.getSharedPreferences("SpinnerPosition", 0).edit();
                prefEditor.putInt("userChoiceSpinner", userChoice);
                prefEditor.commit();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        SharedPreferences sharedPref = getSharedPreferences("SpinnerPosition", 0);
        ((Switch) findViewById(R.id.modeSwitch)).setChecked(sharedPref.getBoolean("SwitchPos", false));
        int spinnerValue = sharedPref.getInt("userChoiceSpinner", -1);
        if (spinnerValue != -1) {
            ((Spinner) findViewById(R.id.inst)).setSelection(spinnerValue);
        }
        int spinnerValueGr = sharedPref.getInt("userChoiceSpinnerGr", -1);
        if (spinnerValueGr != -1 && spinnerValueGr <= mList.size()) {
            ((Spinner) findViewById(R.id.gr)).setSelection(spinnerValueGr);
        }
    }

    public void startTask(View v) {
        new getTT(this, null).execute(new String[]{instNameToUrlName(((Spinner) findViewById(R.id.inst)).getSelectedItem().toString()), ((Spinner) findViewById(R.id.gr)).getSelectedItem().toString(), Integer.toString(currentWeek)});
        int userChoice = ((Spinner) findViewById(R.id.gr)).getSelectedItemPosition();
        Editor prefEditor = getSharedPreferences("SpinnerPosition", 0).edit();
        prefEditor.putInt("userChoiceSpinnerGr", userChoice);
        prefEditor.commit();
    }

    public String instNameToUrlName(String inst) {
        String institute = "";
        Object obj = -1;
        switch (inst.hashCode()) {
            case -2079832143:
                if (inst.equals("История и философия")) {
                    obj = 18;
                    break;
                }
                break;
            case -2071778975:
                if (inst.equals("Уголовный процесс и криминалистика")) {
                    obj = 32;
                    break;
                }
                break;
            case -1911364336:
                if (inst.equals("Теплогазоснабжение, вентиляция, водоснабжение и водоотведение")) {
                    obj = 27;
                    break;
                }
                break;
            case -1556427059:
                if (inst.equals("История государства и права")) {
                    obj = 34;
                    break;
                }
                break;
            case -1457917419:
                if (inst.equals("Начертательная геометрия")) {
                    obj = 30;
                    break;
                }
                break;
            case -1173669703:
                if (inst.equals("Декоративно-прикладное искусство")) {
                    obj = 29;
                    break;
                }
                break;
            case -1040215350:
                if (inst.equals("Живопись и художественное образование")) {
                    obj = 28;
                    break;
                }
                break;
            case -913713696:
                if (inst.equals("Педагогика и методики преподавания")) {
                    obj = 17;
                    break;
                }
                break;
            case -906200659:
                if (inst.equals("Промышленное и гражданское строительство")) {
                    obj = 26;
                    break;
                }
                break;
            case -904534100:
                if (inst.equals("Дизайн и инженерная графика")) {
                    obj = 25;
                    break;
                }
                break;
            case -828520381:
                if (inst.equals("ИНМАШ(маг)")) {
                    obj = 7;
                    break;
                }
                break;
            case -763953174:
                if (inst.equals("Журналистика")) {
                    obj = 22;
                    break;
                }
                break;
            case -612026776:
                if (inst.equals("Теория и методика преподавания иностранных языков и культур")) {
                    obj = 24;
                    break;
                }
                break;
            case -3160126:
                if (inst.equals("Общая и теоретическая физика")) {
                    obj = 14;
                    break;
                }
                break;
            case 33543:
                if (inst.equals("ИП")) {
                    obj = 8;
                    break;
                }
                break;
            case 1033255:
                if (inst.equals("АСИ")) {
                    obj = 1;
                    break;
                }
                break;
            case 86458276:
                if (inst.equals("Высшая математика и математическое образование (51)")) {
                    obj = 15;
                    break;
                }
                break;
            case 149699908:
                if (inst.equals("Русский язык литература и лингвокриминалистика")) {
                    obj = 21;
                    break;
                }
                break;
            case 150118752:
                if (inst.equals("Прикладная математика и информатика")) {
                    obj = 13;
                    break;
                }
                break;
            case 261701351:
                if (inst.equals("Теория государства и права")) {
                    obj = 33;
                    break;
                }
                break;
            case 362895724:
                if (inst.equals("Теоретическая и прикладная психология")) {
                    obj = 20;
                    break;
                }
                break;
            case 507155360:
                if (inst.equals("Теория и практика перевода")) {
                    obj = 23;
                    break;
                }
                break;
            case 716999478:
                if (inst.equals("АСИ(Д)")) {
                    obj = 2;
                    break;
                }
                break;
            case 803539256:
                if (inst.equals("Социология")) {
                    obj = 19;
                    break;
                }
                break;
            case 939640501:
                if (inst.equals("ИИиДПИ")) {
                    obj = 5;
                    break;
                }
                break;
            case 995825797:
                if (inst.equals("ГУМПИ")) {
                    obj = 12;
                    break;
                }
                break;
            case 1000242346:
                if (inst.equals("ИМФИТ")) {
                    obj = null;
                    break;
                }
                break;
            case 1000264207:
                if (inst.equals("ИНМАШ")) {
                    obj = 6;
                    break;
                }
                break;
            case 1000471063:
                if (inst.equals("ИФКИС")) {
                    obj = 10;
                    break;
                }
                break;
            case 1000490316:
                if (inst.equals("ИФЭиУ")) {
                    obj = 11;
                    break;
                }
                break;
            case 1000529696:
                if (inst.equals("ИХиИЭ")) {
                    obj = 9;
                    break;
                }
                break;
            case 1000767664:
                if (inst.equals("ИЭиЭ:")) {
                    obj = 3;
                    break;
                }
                break;
            case 1758909116:
                if (inst.equals("Гражданское право,гражданский процесс и трудовое право")) {
                    obj = 35;
                    break;
                }
                break;
            case 1869920005:
                if (inst.equals("ГУМПИ(2Г)")) {
                    obj = 4;
                    break;
                }
                break;
            case 1905218710:
                if (inst.equals("Нанотехнологии, материаловедение и механика")) {
                    obj = 31;
                    break;
                }
                break;
            case 1925517041:
                if (inst.equals("Дошкольная педагогика и психология")) {
                    obj = 16;
                    break;
                }
                break;
        }
        switch (obj) {
            case null:
                return "IMFIT";
            case 1:
                return "ASI";
            case 2:
                return "D";
            case 3:
                return "IEIE";
            case 4:
                return "PF";
            case 5:
                return "IIiDPI";
            case 6:
                return "IM";
            case 7:
                return "G";
            case 8:
                return "IP";
            case 9:
                return "IHiIE";
            case 10:
                return "IFKiC";
            case 11:
                return "IFEiU";
            case 12:
                return "GYMPI";
            case 13:
                return "48";
            case 14:
                return "12";
            case 15:
                return "51";
            case 16:
                return "17";
            case 17:
                return "18";
            case 18:
                return "22";
            case 19:
                return "23";
            case 20:
                return "25";
            case 21:
                return "27";
            case 22:
                return "29";
            case 23:
                return "30";
            case 24:
                return "31";
            case 25:
                return "39";
            case 26:
                return "38";
            case 27:
                return "40";
            case 28:
                return "34";
            case 29:
                return "33";
            case 30:
                return "32";
            case 31:
                return "15";
            case 32:
                return "68";
            case 33:
                return "63";
            case 34:
                return "67";
            case 35:
                return "66";
            default:
                return institute;
        }
    }

    public void weekp(View v) {
        if (currentWeek > 1) {
            currentWeek--;
            ((TextView) findViewById(R.id.week)).setText("Неделя №" + Integer.toString(currentWeek));
            startTask(v);
        }
    }

    public void weekn(View v) {
        currentWeek++;
        ((TextView) findViewById(R.id.week)).setText("Неделя №" + Integer.toString(currentWeek));
        startTask(v);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Access modifiers changed, original: 0000 */
    public void writeWordsToFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput("groups", 0)));
            for (int i = 0; i < mList.size(); i++) {
                bw.write((String) mList.get(i));
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void readWordsFromFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("groups")));
            String str = "";
            while (true) {
                str = br.readLine();
                if (str != null) {
                    mList.add(str);
                } else {
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void writeTtToFile(String name, List<String> data) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(name, 0)));
            for (int i = 0; i < data.size(); i++) {
                bw.write((String) data.get(i));
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public List<String> readTtFromFile(String name) {
        List<String> help = new ArrayList();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(name)));
            String str = "";
            while (true) {
                str = br.readLine();
                if (str == null) {
                    break;
                }
                help.add(str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return help;
    }

    public void saveTtToFile(View v) {
        writeTtToFile("mon", weektt.mon);
        writeTtToFile("tue", weektt.tue);
        writeTtToFile("wed", weektt.wed);
        writeTtToFile("thu", weektt.thu);
        writeTtToFile("fri", weektt.fri);
        writeTtToFile("sat", weektt.sat);
        writeTtToFile("days", weektt.days);
    }

    public void loadTtFromFile(View v) {
        weektt = new Weektt();
        weektt.mon = readTtFromFile("mon");
        weektt.tue = readTtFromFile("tue");
        weektt.wed = readTtFromFile("wed");
        weektt.thu = readTtFromFile("thu");
        weektt.fri = readTtFromFile("fri");
        weektt.sat = readTtFromFile("sat");
        weektt.days = readTtFromFile("days");
        this.mSectionsPagerAdapter.notifyDataSetChanged();
        if (!weektt.days.isEmpty()) {
            findViewById(R.id.tabs).setVisibility(0);
            ((TextView) findViewById(R.id.week)).setText("Неделя №" + Integer.toString(currentWeek));
            findViewById(R.id.tabs).setVisibility(0);
            this.mViewPager.setCurrentItem(Calendar.getInstance().get(7) - 2, true);
        }
    }
}
