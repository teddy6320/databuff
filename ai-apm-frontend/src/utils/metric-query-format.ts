type AggregatorType =  'avg' | 'sum' | 'max' | 'min' | 'count' | 'last';
type OperatorType = '=' | '!=' | 'empty' | 'notEmpty' | 'inList' | 'notInList' | 'like' | 'notLike' | 'startWith' | 'endWith' | 'regEx';
type ConnectorType = 'AND' | 'OR';

type SimpleFromCondition = [string, OperatorType, string, ConnectorType];

interface FromCondition {
  left: string;
  operator: OperatorType;
  right: string;
  connector: ConnectorType;
}

export interface MetricBaseQueryParams {
  aggs: AggregatorType;
  from: FromCondition[];
  by: string[];
  metric: string;
  types: string[];
  start: number;
  end: number;
  interval: number;
}

export const formatMetricBaseQuery = (
  metric: string = '',
  from: SimpleFromCondition[] = [],
  start: number = Date.now() - 3600000,
  end: number = Date.now(),
  interval: number = 60,
  aggs: AggregatorType = 'avg',
  by: string[] = [],
  types: string[] = [],
): MetricBaseQueryParams => {
  const formattedFrom: FromCondition[] = from.map(condition => ({
    left: condition[0],
    operator: condition[1],
    right: condition[2],
    connector: condition[3] || 'AND'
  }));
  return {
    aggs,
    from: formattedFrom,
    by,
    metric,
    types,
    start,
    end,
    interval
  }
}
