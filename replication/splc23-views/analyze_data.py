import os
import sys
import numpy as np
import pandas as pd
import scipy.stats as stats
import math

def prepare_data():
    plot_dir = root_dir_name + '_plots'
    if os.path.isdir(plot_dir):
        df = pd.read_csv(plot_dir + '/merged.csv', sep = ';')
    else:
        df_repos = []
        subfolders= [f.path for f in os.scandir(root_dir_name) if f.is_dir()]
        for subfolder in subfolders:
            file_names= [f.path for f in os.scandir(subfolder) if f.is_file() and f.path.endswith('.csv')]
            repo = os.path.basename(os.path.normpath(subfolder))
            print(repo)
            df_parts = []
            for file_name in file_names:
                print('\t'+file_name)
                df_parts.append(pd.read_csv(file_name, sep = ';'))
            df_merged = pd.concat(df_parts)
            df_merged['repo'] = repo
            df_repos.append(df_merged)
        df = pd.concat(df_repos)

        os.makedirs(plot_dir, exist_ok=True)
        df.to_csv(plot_dir + '/merged.csv', index=False, sep = ';')
    return df


def set_graphics_options():
    pd.set_option('display.max_columns', None)
    pd.set_option('display.max_rows', None)
    pd.set_option('display.max_colwidth', None)


def create_csv(df, name):
    if show_results:
        print(df)

    if save_results:
        df.to_csv(root_dir_name + '_plots/' + name + '.csv', index=False, sep = ';')


def create_table(df, name):
    table = df.style.format(decimal='.', thousands=',', precision=2, escape="latex").to_latex(multicol_align='c')

    if show_results:
        print(table)

    if save_results:
        with open(root_dir_name + '_plots/'+name+'.tex', 'w') as f:
            print(table, file=f)


def sig_test(df):
    print(df[['vtype','msoptimized','msnaive']].head())
    sig = "Nan"
    siglog = 0
    effect_size = 0
    mean_naive = df['msnaive'].mean()
    mean_opt = df['msoptimized'].mean()
    mean_diff = df['diff'].mean()
    if len(df['msnaive']) > 3:
        shapiro_stat, shapiro_p = stats.shapiro(df['diff'])
        if shapiro_p < 0.05:
            wilcoxon_stat, wilcoxon_p = stats.wilcoxon(df['msnaive'], df['msoptimized'])
            effect_size = wilcoxon_stat
            if not np.isnan(wilcoxon_p):
                if wilcoxon_p < 0.005:
                    sig = "True"
                else:
                    sig = "False"
                if wilcoxon_p > 0:
                    siglog = math.trunc(math.log10(wilcoxon_p))
                else:
                    siglog = -1000
    return pd.Series({'sig':sig, 'siglog':siglog, 'effect_size':effect_size, 'mean_naive':mean_naive, 'mean_opt':mean_opt, 'mean_diff':mean_diff})


if __name__ == "__main__":
    if len(sys.argv) < 2:
        root_dir_name = '23_04_20'
    else:
        root_dir_name = sys.argv[1]
    print(root_dir_name)

    save_results = True
    show_results = True

    set_graphics_options()
    df_data = prepare_data()

    bins = [0,2,11,1001,60001,600001,3600001]
    lables= {1:'1ms', 2:'10ms', 3:'1s', 4:'1min', 5:'10min', 6:'1h'}

    df_data['diff'] = df_data['msnaive'] - df_data['msoptimized']
    df_data['rel'] = df_data['msnaive'].div(df_data['msoptimized'])

    df_count = df_data['msnaive'].count()
    data_type_count = df_data.groupby('vtype')[['msnaive']].count().reset_index()
    data_type_count.columns = ['vtype','count_per_vtype']
    data_type_count.loc[len(data_type_count)] = ["all", df_count]
    create_csv(data_type_count, 'count')

    df_grouped_naive = df_data.copy().sort_values('msnaive')
    df_grouped_naive = df_grouped_naive.groupby(['vtype',np.digitize(df_grouped_naive['msnaive'], bins)])

    df_grouped_opt = df_data.copy().sort_values('msoptimized')
    df_grouped_opt = df_grouped_opt.groupby(['vtype',np.digitize(df_grouped_opt['msoptimized'], bins)])

    df_ttest = df_grouped_opt
    df_ttest = df_ttest.apply(lambda dfx: sig_test(dfx)).reset_index()
    df_ttest.columns = ['Type', 'Time', 'Sig', 'SigLog', 'EffectSize', 'NaiveMean', 'OptMean', 'DiffMean']
    df_ttest = df_ttest[['Type', 'Time', 'OptMean', 'NaiveMean', 'DiffMean', 'Sig', 'SigLog', 'EffectSize']]
    df_ttest = df_ttest.replace({'Time': lables})
    #df_ttest = df_ttest.set_index(['Type', 'Time'])
    create_csv(df_ttest, 'wilcoxon')

    df_compare_count = df_data.copy()
    df_compare_count = df_compare_count[df_compare_count['msnaive'] >= 1]
    df_compare_count = df_compare_count[df_compare_count['msoptimized'] >= 1]
    print('naive < opt: ' + str(df_compare_count[df_compare_count['msnaive'] < df_compare_count['msoptimized']].count()[0]))
    print('naive = opt: ' + str(df_compare_count[df_compare_count['msnaive'] == df_compare_count['msoptimized']].count()[0]))
    print('naive > opt: ' + str(df_compare_count[df_compare_count['msnaive'] > df_compare_count['msoptimized']].count()[0]))

    df_speedup = df_data.copy()
    df_speedup = df_speedup[df_speedup['msnaive'] >= 1000]
    #print(df_speedup[['rel','diff','msnaive','msoptimized']].head(100))
    df_speedup = df_speedup[['vtype','rel','diff']]
    df_speedup = df_speedup.groupby('vtype').agg(['mean','median','min','max']).reset_index()
    create_csv(df_speedup, 'rel_speedup1sOrMore')

    df_hist = df_grouped_naive[['msnaive']].count().join(df_grouped_opt[['msoptimized']].count()).reset_index().fillna(0)
    df_hist = df_hist.join(data_type_count.set_index('vtype'), on='vtype')
    df_hist['NaiveRel'] = 100*df_hist['msnaive'].div(df_hist['count_per_vtype'])
    df_hist['OptRel'] = 100*df_hist['msoptimized'].div(df_hist['count_per_vtype'])
    df_hist.columns = ['Type', 'Time', 'Naive', 'Opt', 'count_per_vtype', 'NaiveRel', 'OptRel']
    df_hist = df_hist[['Type', 'Time', 'Opt', 'OptRel', 'Naive', 'NaiveRel']]
    df_hist = df_hist.astype({'Naive':'int','Opt':'int'})
    df_hist = df_hist.replace({'Time': lables})
    df_hist['Type'] = df_hist['Type'].apply(lambda name: 'view' + name)
    df_hist.columns = df_hist.columns.map(lambda name: 'view' + name)
    df_hist = df_hist.set_index(['viewType', 'viewTime'])
    create_table(df_hist, 'hist')

    df_median = df_data.copy()
    df_median = df_median.groupby('vtype')[['msnaive', 'msoptimized']].median().reset_index()
    df_median.columns = ['Type', 'Naive', 'Opt']
    df_median = df_median[['Type', 'Opt', 'Naive']]
    df_median = df_median.set_index(['Type'])
    create_table(df_median, 'median')

    df_rank = df_data.copy().set_index(['repo','commit','file','vtype'])
    df_rank['msnaive_rank'] = df_rank['msnaive'].rank(ascending=False)
    df_rank['msoptimized_rank'] = df_rank['msoptimized'].rank(ascending=False)

    df_rank_naive = df_rank.sort_values('msnaive_rank').head(10)
    df_rank_opt = df_rank.sort_values('msoptimized_rank').head(10)

    df_rank = pd.concat([df_rank_naive,df_rank_opt])
    df_rank = df_rank[~df_rank.index.duplicated(keep='first')].reset_index()
    create_csv(df_rank, 'rank')
