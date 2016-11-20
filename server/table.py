class table:

    def __init__(self,cols=None):
        self.rows = []
        self.columns = cols

    def __repr__(self):
        ret = ''
        for i in range(len(self.rows)):
            ret = ret + 'Row {}: '.format(i+1)
            for key in self.rows[i]:
                ret = ret + "-|{} = '{}'|-".format(key,self.rows[i][key])
            ret = ret + '\n'
        return ret

    def add_row(self,vals = []):
        # Akceptuje zoznam tuplov (stlpec,hodnota)
        row = dict()
        for i in vals:
            ind,val = i[0],i[1]
            if ind in self.columns:
                row[ind] = val
            else:
                print("Column '{}' does not exist".format(ind))
                return False
        for c in self.columns:
            if not c in row.keys():
                row[c] = None
        self.rows.append(row)
        return True

    def import_rows(self,rows):
        for i in rows:
            self.rows.append(i)

    def get_rows_where(self,conds=[]):
        # Berie zoznam dvojic (funkcia,stlpec,hodnota) a vrati zoznam riadkov,
        # v ktorych hodnota pre kazdy stlpec splna danu funkciu.
        # napr.: .get_rows_where([ (not eq,"Meno","None"), (eq,"AccessLevel","2") ])
        ret = []
        for i in self.rows:
            t = True
            for c in conds:
                cond = c[0](i[c[1]],c[2])
                if i[c[1]] is None or not cond:
                    t = False
            if t == True:
                ret.append(i)
        return ret

    def write(self,path):
        with open(path,'w') as f:
            for r in self.rows:
                f.write(str(r) + '\n')

    def import_table(self,path):
        self.rows = []
        with open(path,'r') as f:
            for r in f.readlines():
                if '\n' in r:
                    r = r[:-1]
                if r != '' and r != '\n':
                    self.rows.append(eval(r))
        self.columns = list(self.rows[0].keys())












"""
test = table(['Name','Gender','Age'])
test.add_row([('Name','majo'),('Gender','M'),('Age',3)])
test.add_row([('Name','majo'),('Gender','M'),('Age',4)])
test.add_row([('Name','majo'),('Gender','M'),('Age',5)])
test.add_row([('Name','jakub'),('Gender','M'),('Age',3)])
test.add_row([('Name','jakub'),('Gender','M'),('Age',4)])
test.add_row([('Name','jakub'),('Gender','M'),('Age',5)])
test.add_row([('Name','matej'),('Gender','M'),('Age',3)])
test.add_row([('Name','matej'),('Gender','M'),('Age',4)])
test.add_row([('Name','matej'),('Gender','M'),('Age',5)])
test.write('test/test.txt')
print(test,test.columns)
test1 = table(['Name','Gender','Age'])
test1.import_table('test/test.txt')
print(test1.columns==test.columns)
"""

































