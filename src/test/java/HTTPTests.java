import org.junit.Test;

public class HTTPTests {

    @Test
    public void testHooks() throws Exception {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1251\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"template_css.css\"/>\n" +
                "<title></title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "\n" +
                "�������: <span style=\"font-weight:bold;color:#90012C;\">�������� ������ ���������</span><br/>\n" +
                "������: ���-118 ()<br/>   <br/>\n" +
                "<table class=\"contentpaneopen\"  border=\"2\">\n" +
                "<tr >\n" +
                "\t<td><strong><center>1 ����</center></strong></td>\n" +
                "\t<td><strong><center>2 ����</center></strong></td>\n" +
                "\t<td><strong><center>3 ����</center></strong></td>\n" +
                "\t<td><strong><center>4 ����</center></strong></td>\n" +
                "\t<td><strong><center>5 ����</center></strong></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "        <td><a href=\"./?semestr=1\">1 �������</a></td>\n" +
                "            <td><strong>3 �������</strong></td>\n" +
                "        <td>5 �������</td>\n" +
                "    <td>7 �������</td>\n" +
                "    <td>9 �������</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "        <td><a href=\"./?semestr=2\">2 �������</a></td>\n" +
                "        <td>4 �������</td>\n" +
                "    <td>6 �������</td>\n" +
                "    <td>8 �������</td>\n" +
                "    <td>10 �������</td>\n" +
                "</tr>\n" +
                "\n" +
                "</table>\n" +
                "<table class=\"contentpaneopen\" border=\"2\">\n" +
                "\t<tr>\n" +
                "\t\t\t<td width=\"50%\" align=\"center\">\n" +
                "\t\t\t<a href=\"./?cur_m=1\">������� ������</a>\n" +
                "\t\t</td>\n" +
                "\t\t<td width=\"50%\" align=\"center\"><strong>�������� ������</strong></td>\n" +
                "\t\t</tr>\n" +
                "</table>\n" +
                "\t<table class=\"contentpaneopen\" border=\"2\">\n" +
                "\t<tr>\n" +
                "\t\t<td width=\"75%\" style=\"border-bottom:2px solid black;\">�������</td>\n" +
                "\t\t\t\t<td align=\"center\" width=\"25%\" style=\"border-bottom:2px solid black;\">��� �������������</td>\n" +
                "\t\t\t\t<td align=\"center\" width=\"0%\" style=\"border-bottom:2px solid black; border-right:2px solid black; font-size:9px;\">����</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" width=\"6%\" style=\"border-bottom:2px solid black; font-size:9px;\">��1</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"6%\" style=\"border-bottom:2px solid black; font-size:9px;\">��1</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"6%\" style=\"border-bottom:2px solid black; font-size:9px;\">��2</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"6%\" style=\"border-bottom:2px solid black; border-right:2px solid black; font-size:9px;\">��2</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"7%\" style=\"border-bottom:2px solid black; border-right:2px solid black; font-size:9px;\"><strong>�����</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"5%\" style=\"border-bottom:2px solid black; font-size:8px; \">���</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"5%\" style=\"border-bottom:2px solid black; font-size:9px;\">���</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"5%\" style=\"border-bottom:2px solid black; font-size:9px; \">�1</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"5%\" style=\"border-bottom:2px solid black; border-right:2px solid black; font-size:9px; \">�2</td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"5%\" style=\"border-bottom:2px solid black; border-right:2px solid black; font-size:9px; \"><strong>���-�� ������</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" width=\"5%\" style=\"border-bottom:2px solid black; font-size:9px;\"><strong>����</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;\">\n" +
                "\t\t\t1. ��������� �� ��������� �����\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">�������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">120</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">60</td>\n" +
                "\t\t\t\t\t<td align=\"center\">35</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>95</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">5</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>95  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"background:#CCCCCC;\"><strong>5</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;font-weight: bold\">\n" +
                "\t\t\t2. ����������� ����'�����\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">150</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">19</td>\n" +
                "\t\t\t\t\t<td align=\"center\">15</td>\n" +
                "\t\t\t\t\t<td align=\"center\">23</td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\">17</td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>74</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">2</td>\n" +
                "\t\t\t\t\t<td align=\"center\">15</td>\n" +
                "\t\t\t\t\t<td align=\"center\">4</td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>89  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"background:#CCCCCC;\"><strong>4</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;font-weight: bold\">\n" +
                "\t\t\t3. ��������� �������\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">120</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">60</td>\n" +
                "\t\t\t\t\t<td align=\"center\">36</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>96</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">2</td>\n" +
                "\t\t\t\t\t<td align=\"center\">70</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>166  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"background:#CCCCCC;\"><strong>4</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;\">\n" +
                "\t\t\t4. Գ����\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">̳������ �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">150</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">18</td>\n" +
                "\t\t\t\t\t<td align=\"center\">9</td>\n" +
                "\t\t\t\t\t<td align=\"center\">27</td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\">8</td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>62</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">3</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>62  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"background:#CCCCCC;\"><strong>3</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;\">\n" +
                "\t\t\t5. Գ�������\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">90</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">46</td>\n" +
                "\t\t\t\t\t<td align=\"center\">30</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>76</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">4</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>76  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"background:#CCCCCC;\"><strong>4</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;\">\n" +
                "\t\t\t6. ���� �����\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">90</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">60</td>\n" +
                "\t\t\t\t\t<td align=\"center\">40</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>100</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">���.</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>100  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" ><strong>���.</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;\">\n" +
                "\t\t\t7. �������� ���� (�� ���������� ������������)\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">ĳ������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">60</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">27</td>\n" +
                "\t\t\t\t\t<td align=\"center\">12</td>\n" +
                "\t\t\t\t\t<td align=\"center\">14</td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\">15</td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>68</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">���.</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>68  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" ><strong>���.</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;\">\n" +
                "\t\t\t8. ��'�����-��������� �������������\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">�������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">120</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">60</td>\n" +
                "\t\t\t\t\t<td align=\"center\">40</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>100</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">���.</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>100  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" ><strong>���.</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr >\n" +
                "\t\t<td style=\"font-size:10px;\">\n" +
                "\t\t\t9. ������� ������\n" +
                "\t\t</td>\n" +
                "\t\t\t\t\t<td style=\"font-size:10px;\">��������� �. �.</td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; font-size:9px;\">90</td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">54</td>\n" +
                "\t\t\t\t\t<td align=\"center\">30</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>84</strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\">���.</td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black;\"><strong>84  </strong></td>\n" +
                "\t\t\t\t\t<td align=\"center\" ><strong>���.</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t\t<tr>\n" +
                "\t\t<td style=\"font-size:10px; font-weight: bold; border-top:2px solid black;\">������� ���</td>\n" +
                "\t\t\t\t<td style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"><strong>990</strong></td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"><strong>4</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t<tr>\n" +
                "\t\t<td style=\"font-size:10px; font-weight: bold; border-top:2px solid black;\">��������� ������� ���</td>\n" +
                "\t\t\t\t<td style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t  <td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"><strong>1620</strong></td>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-right:2px solid black; border-top:2px solid black;\"></td>\n" +
                "\t\t\t\t\t<td align=\"center\" style=\"border-top:2px solid black;\"><strong>4,07</strong></td>\n" +
                "\t\t\t\t\t\t\t\t</tr>\n" +
                "\t</table>\n" +
                "    <p align=\"center\"><input type=\"button\" onclick=\"window.location='./?action=logout'\" value=\"�����\" class=\"button\"/></p>\n" +
                "</body>\n" +
                "</html>";

        System.out.println(html.replaceAll("<head>.*?</head>", "test"));
    }

}
